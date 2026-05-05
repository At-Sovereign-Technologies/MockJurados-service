# Mock Jurados Service — Guía de Integración

> **Módulo mock de "Gestión de Jurados de Votación"** para el *Sistema Registraduría (SR)* de Sello Legítimo.
>
> Comportamiento determinístico, stateful e inspeccionable. Diseñado para desarrollo, integración y demos.

---

## 1. Arquitectura

```
┌─────────────────────────────────────────────────────────┐
│                   MockJurados-service                    │
│                                                          │
│  ┌──────────┐  ┌───────────┐  ┌──────────────────────┐  │
│  │ REST API  │  │  gRPC    │  │  In-Memory State      │  │
│  │ :8083     │  │  :9091   │  │  (ConcurrentHashMap)  │  │
│  │           │  │           │  │                       │  │
│  │ • Sorteo  │  │ • SITE   │  │ • Jurados             │  │
│  │ • Excusas │  │   gRPC   │  │ • Mesas               │  │
│  │ • Asist.  │  │   Server │  │ • Excusas             │  │
│  │ • Debug   │  │          │  │ • Asistencia          │  │
│  └────┬──────┘  └────┬─────┘  │ • Outbox (eventos)    │  │
│       │              │        └──────────────────────┘  │
│       │              │                                    │
│       │              └──────────┐                         │
│       │                         │                         │
│       ▼                         ▼                         │
│  ┌────────────────────────────────────────────────────┐  │
│  │  gRPC Client → ConfiguracionEleccion (:9090)       │  │
│  │  (Valida eleccionId antes del sorteo)               │  │
│  └────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

### Componentes

| Componente | Rol |
|---|---|
| `ControladorJurados` | REST API funcional (sorteo, excusa, asistencia, consultas) |
| `ControladorMockDebug` | REST API de depuración (/mock/*, /mock/reset) |
| `ServicioJurados` | Lógica de sorteo determinístico y reemplazo |
| `ServicioExcusa` | Ciclo de vida completo de excusas (PENDIENTE → APROBADA/RECHAZADA → REEMPLAZO) |
| `ServicioAsistencia` | Control de asistencia con auto-reemplazo en AUSENTE |
| `ServicioOutbox` | Cola de eventos para integración con SITE |
| `GeneradorDeterministico` | Generación seeded de nombres, cédulas y datos colombianos |
| `JuradoGrpcService` | Servidor gRPC para SITE |
| `GrpcConfiguracionClient` | Cliente gRPC a ConfiguracionEleccion-service |

---

## 2. Data Model

### Jurado

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | UUID | Identificador único |
| `cedula` | String | Cédula determinística (8-10 dígitos) |
| `nombre` | String | Nombre de pila (pool colombiano) |
| `apellido` | String | Apellido (pool colombiano) |
| `mesaId` | String | Mesa asignada |
| `puestoId` | String | Puesto de votación |
| `rol` | Enum | `PRESIDENTE`, `SECRETARIO`, `VOCAL_1`, `VOCAL_2` |
| `estado` | Enum | `ASIGNADO`, `EXCUSADO`, `REEMPLAZADO`, `ACTIVO` |
| `reemplazaA` | UUID | (Opcional) Jurado original al que reemplaza |

### Mesa

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | UUID | Identificador único |
| `numero` | int | Número de mesa (1..N) |
| `puestoId` | String | Puesto de votación |
| `departamento` | String | Departamento |
| `municipio` | String | Municipio |
| `juradoIds` | List\<UUID\> | Jurados asignados |

### Excusa

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | UUID | Identificador único |
| `juradoId` | UUID | Jurado que se excusa |
| `mesaId` | String | Mesa del jurado |
| `motivo` | String | Razón de la excusa |
| `documentoSoporte` | String | Documento anexo (opcional) |
| `estado` | Enum | `PENDIENTE`, `APROBADA`, `RECHAZADA` |
| `juradoReemplazoId` | UUID | (Si APROBADA) Jurado de reemplazo |
| `motivoRechazo` | String | (Si RECHAZADA) Motivo del rechazo |

### Asistencia

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | UUID | Identificador único |
| `juradoId` | UUID | Jurado |
| `mesaId` | String | Mesa |
| `estado` | Enum | `PENDIENTE`, `PRESENTE`, `AUSENTE` |
| `observacion` | String | Nota opcional |

### EventoOutbox

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | UUID | Identificador único |
| `tipo` | Enum | `JURADO_ASIGNADO`, `EXCUSA_RESUELTA`, `ASISTENCIA_REGISTRADA`, `REEMPLAZO_GENERADO` |
| `payload` | String (JSON) | Datos del evento |
| `emitido` | boolean | Marca de enviado a SITE |

---

## 3. API REST

### 3.1 Sorteo de Jurados

```
POST /api/jurados/sorteo
```

**Request:**
```json
{
  "eleccionId": "1",
  "departamento": "Cundinamarca",
  "municipio": "Bogota",
  "numeroMesas": 3,
  "juradosPorMesa": 4,
  "seed": 42
}
```

**Response (201):**
```json
{
  "eleccionId": "1",
  "departamento": "Cundinamarca",
  "municipio": "Bogota",
  "seed": 42,
  "totalMesas": 3,
  "totalJurados": 12,
  "mesas": [
    {
      "id": "a1b2c3d4-...",
      "numero": 1,
      "jurados": [
        {
          "id": "e5f6g7h8-...",
          "cedula": "45123456",
          "nombre": "Carlos",
          "apellido": "Garcia",
          "mesaId": "a1b2c3d4-...",
          "puestoId": "PUESTO-CUN-001",
          "rol": "PRESIDENTE",
          "estado": "ASIGNADO",
          "reemplazaA": null,
          "fechaCreacion": "2026-05-05T15:30:00Z"
        }
      ]
    }
  ]
}
```

> **Determinismo**: Mismo `seed` + `eleccionId` + `departamento` + `municipio` → mismos jurados.

---

### 3.2 Excusas

**Presentar excusa:**
```
POST /api/jurados/excusa
```
```json
{
  "juradoId": "e5f6g7h8-...",
  "mesaId": "a1b2c3d4-...",
  "motivo": "Enfermedad comprobada",
  "documentoSoporte": "url-al-certificado-medico"
}
```

**Resolver excusa:**
```
POST /api/jurados/excusa/{id}/resolver
```
```json
{
  "estado": "APROBADA",
  "motivoRechazo": null
}
```

**Comportamiento:**
| Resolución | Efecto |
|---|---|
| `APROBADA` | Jurado original → `REEMPLAZADO`. Se genera nuevo jurado automáticamente con el mismo rol. |
| `RECHAZADA` | Jurado original → `ASIGNADO`. Se registra `motivoRechazo`. |

**Response:**
```json
{
  "id": "x1y2z3-...",
  "juradoId": "e5f6g7h8-...",
  "estado": "APROBADA",
  "juradoReemplazoId": "r1s2t3-...",
  "fechaSolicitud": "2026-05-05T16:00:00Z",
  "fechaResolucion": "2026-05-05T16:05:00Z"
}
```

---

### 3.3 Asistencia

```
POST /api/jurados/asistencia
```
```json
{
  "juradoId": "e5f6g7h8-...",
  "mesaId": "a1b2c3d4-...",
  "estado": "AUSENTE",
  "observacion": "No se presento a las 8:00 AM"
}
```

> Si `estado = AUSENTE`, se genera automáticamente un jurado de reemplazo.

---

### 3.4 Consultas

```
GET /api/jurados/mesa/{mesaId}     → Jurados de una mesa
GET /api/jurados/{id}               → Jurado por ID
GET /api/jurados/cedula/{cedula}    → Jurado por cédula
```

---

### 3.5 Debug / Mock Endpoints

| Método | Path | Descripción |
|---|---|---|
| `GET` | `/mock/jurados` | Todos los jurados |
| `GET` | `/mock/excusas` | Todas las excusas |
| `GET` | `/mock/asistencias` | Todos los registros de asistencia |
| `GET` | `/mock/outbox` | Todos los eventos emitidos |
| `GET` | `/mock/state` | Resumen de estado completo |
| `DELETE` | `/mock/reset` | **Reinicia todo el estado del mock** |

**`/mock/state`:**
```json
{
  "totalJurados": 12,
  "totalExcusas": 3,
  "totalAsistencias": 0,
  "totalEventosOutbox": 15,
  "timestamp": "2026-05-05T15:35:00Z"
}
```

---

## 4. gRPC Contract (para SITE)

**Proto:** `src/main/proto/jurados.proto`

```protobuf
service JuradoService {
  rpc ConsultarJuradosPorMesa(ConsultarJuradosPorMesaRequest) returns (ConsultarJuradosPorMesaResponse);
  rpc ConsultarJuradoPorCedula(ConsultarJuradoPorCedulaRequest) returns (JuradoDetalle);
  rpc ConsultarAsistenciaPorMesa(ConsultarAsistenciaPorMesaRequest) returns (ConsultarAsistenciaPorMesaResponse);
  rpc SuscribirEventos(SuscribirEventosRequest) returns (stream EventoJurado);
}
```

| Método | Tipo | Descripción |
|---|---|---|
| `ConsultarJuradosPorMesa` | Unary | Obtiene jurados de una mesa |
| `ConsultarJuradoPorCedula` | Unary | Obtiene detalle de jurado por cédula |
| `ConsultarAsistenciaPorMesa` | Unary | Obtiene asistencias de una mesa |
| `SuscribirEventos` | Server Streaming | Recibe eventos en tiempo real (excusas, reemplazos, asistencias) |

**Eventos emitidos:**
| Tipo | Cuándo |
|---|---|
| `JURADO_ASIGNADO` | Durante el sorteo |
| `EXCUSA_RESUELTA` | Al resolver una excusa (APROBADA/RECHAZADA) |
| `ASISTENCIA_REGISTRADA` | Al marcar asistencia |
| `REEMPLAZO_GENERADO` | Cuando se genera un reemplazo (por excusa o ausencia) |

---

## 5. Determinismo Explicado

El generador usa `java.util.Random` con una semilla compuesta:

```java
hash = (seed * 31 + eleccionId.hashCode()) * 31 + departamento.hashCode();
hash = hash * 31 + municipio.hashCode();
Random rng = new Random(hash);
```

**Propiedades:**
- Misma `seed` + `eleccionId` + `departamento` + `municipio` → **idéntico resultado**
- Cada jurado tiene cédula única (validación contra duplicados)
- Los nombres y apellidos se seleccionan de pools realistas colombianos

---

## 6. Flujo de Reemplazo Automático

```
                    ┌──────────────┐
                    │  EXCUSA      │
                    │  PENDIENTE   │
                    └──────┬───────┘
                           │
                    ┌──────▼───────┐
                    │  Resolver    │
                    └──┬───────┬───┘
                       │       │
              APROBADA │       │ RECHAZADA
                       │       │
              ┌────────▼┐  ┌───▼──────┐
              │ Original │  │ Original │
              │→ REEMP.  │  │→ ASIGNADO│
              │          │  └──────────┘
              │ Generar  │
              │ reemplazo│
              └────────┬─┘
                       │
              ┌────────▼──────┐
              │ ASISTENCIA    │
              │ AUSENTE       │
              │→ auto-reemp.  │
              └───────────────┘
```

---

## 7. Integración con otros módulos

### Desde ConfiguracionEleccion-service

No requiere cambios. El `MockJurados-service` actúa como **cliente gRPC** que consume el endpoint `ObtenerEleccion` para validar que el `eleccionId` existe antes de ejecutar un sorteo.

### Desde Gestión Pre-Electoral

Puede consumir la REST API del mock para:
1. **Obtener lista de jurados** por mesa
2. **Registrar asistencia** el día de votación
3. **Consultar excusas** de un jurado

### DevOps: Gateway (Caddy)

El mock expone:
- **REST:** `:8083` (HTTP)
- **gRPC:** `:9091`

Para integrarlo en el gateway, agregar al `Caddyfile`:
```
jurados.sello-legitimo.site {
    reverse_proxy mock-jurados:8083
}
```

El servicio está listado en `docker-compose.local.gateway.yml` como dependencia del gateway.

---

## 8. Cómo ejecutar

### Local (Spring Boot)
```bash
cd MockJurados-service
./mvnw clean compile exec:java -Dspring-boot.run.profiles=dev
# o bien:
./mvnw clean package -DskipTests
java -jar target/MockJurados-0.0.1-SNAPSHOT.jar
```

### Docker Compose
```bash
docker compose -f docker-compose.local.yml up mock-jurados --build
```

### Verificar que funciona
```bash
curl http://localhost:8083/mock/state

# Realizar sorteo de prueba
curl -X POST http://localhost:8083/api/jurados/sorteo \
  -H "Content-Type: application/json" \
  -d '{
    "eleccionId": "1",
    "departamento": "Cundinamarca",
    "municipio": "Bogota",
    "numeroMesas": 2,
    "juradosPorMesa": 4,
    "seed": 42
  }'
```

---

## 9. Contrato de Comportamiento

| Escenario | Comportamiento esperado |
|---|---|
| Misma seed + params → sorteo | Resultado idéntico |
| Jurado ya tiene excusa PENDIENTE | Se rechaza nueva excusa (400) |
| Excusa APROBADA | Genera reemplazo automático, outbox event |
| Excusa RECHAZADA | Jurado vuelve a ASIGNADO |
| Asistencia AUSENTE | Genera reemplazo automático |
| Jurado REEMPLAZADO | No puede presentar nueva excusa |
| GET /mock/reset | Todo el estado se limpia |

---

## 10. Ejemplos de uso con curl

### Sorteo
```bash
curl -s -X POST http://localhost:8083/api/jurados/sorteo \
  -H "Content-Type: application/json" \
  -d '{"eleccionId":"1","departamento":"Antioquia","municipio":"Medellin","numeroMesas":1,"juradosPorMesa":4,"seed":100}' \
  | jq .
```

### Excusa + Resolución
```bash
# 1. Tomar ID de un jurado del sorteo anterior
JURADO_ID="<id-del-jurado>"
MESA_ID="<id-de-la-mesa>"

# 2. Presentar excusa
EXCUSA_RESP=$(curl -s -X POST http://localhost:8083/api/jurados/excusa \
  -H "Content-Type: application/json" \
  -d "{\"juradoId\":\"$JURADO_ID\",\"mesaId\":\"$MESA_ID\",\"motivo\":\"Enfermedad\"}")
EXCUSA_ID=$(echo $EXCUSA_RESP | jq -r '.id')
echo "Excusa creada: $EXCUSA_ID"

# 3. Resolver como APROBADA
curl -s -X POST "http://localhost:8083/api/jurados/excusa/$EXCUSA_ID/resolver" \
  -H "Content-Type: application/json" \
  -d '{"estado":"APROBADA"}' | jq .
```

### Asistencia
```bash
curl -s -X POST http://localhost:8083/api/jurados/asistencia \
  -H "Content-Type: application/json" \
  -d "{\"juradoId\":\"$JURADO_ID\",\"mesaId\":\"$MESA_ID\",\"estado\":\"AUSENTE\"}" | jq .
```

### Debug
```bash
curl -s http://localhost:8083/mock/state | jq .
curl -s http://localhost:8083/mock/jurados | jq '. | length'
```

---

## 11. Preguntas Frecuentes

**¿Por qué in-memory en vez de PostgreSQL?**
El mock es descartable por diseño. Usar `ConcurrentHashMap` simplifica la infraestructura (no requiere base de datos) y permite reinicio instantáneo con `DELETE /mock/reset`.

**¿Cómo sé que el sorteo es determinístico?**
Ejecute el mismo sorteo dos veces con la misma semilla y parámetros. Obtendrá exactamente los mismos jurados (nombres, apellidos, cédulas y roles).

**¿Los datos generados son datos reales?**
No. Todos los nombres, apellidos y cédulas son generados sintéticamente a partir de pools predefinidos y un generador seeded.

**¿Qué pasa si ConfiguracionEleccion-service no está disponible?**
El mock intenta validar `eleccionId` vía gRPC. Si el servicio no responde, el sorteo continúa igual (log de warning). Esto permite desarrollo offline.
