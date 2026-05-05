#!/bin/bash
set -e

BASE_URL="${MOCK_URL:-http://localhost:8083}"

echo "========================================"
echo "MockJurados Service - Test Suite"
echo "Base URL: $BASE_URL"
echo "========================================"
echo ""

# Helper function
function api() {
    local method=$1
    local path=$2
    local body=$3
    if [ -n "$body" ]; then
        curl -s -X "$method" "$BASE_URL$path" -H "Content-Type: application/json" -d "$body"
    else
        curl -s -X "$method" "$BASE_URL$path"
    fi
}

# 1. Reset state
echo "[1/10] Resetting mock state..."
api DELETE "/mock/reset" > /dev/null
echo "OK"
echo ""

# 2. Check empty state
echo "[2/10] Checking initial state..."
api GET "/mock/state" | jq .
echo ""

# 3. Sorteo
echo "[3/10] Running sorteo (seed=42, 2 mesas, 4 jurados/mesa)..."
SORTEO=$(api POST "/api/jurados/sorteo" '{
    "eleccionId": "1",
    "departamento": "Cundinamarca",
    "municipio": "Bogota",
    "numeroMesas": 2,
    "juradosPorMesa": 4,
    "seed": 42
}')
echo "$SORTEO" | jq .
echo ""

MESA_ID=$(echo "$SORTEO" | jq -r '.mesas[0].id')
JURADO_ID=$(echo "$SORTEO" | jq -r '.mesas[0].jurados[0].id')
CEDULA=$(echo "$SORTEO" | jq -r '.mesas[0].jurados[0].cedula')

echo "Extracted: mesaId=$MESA_ID, juradoId=$JURADO_ID, cedula=$CEDULA"
echo ""

# 4. Get jurados by mesa
echo "[4/10] Get jurados by mesa..."
api GET "/api/jurados/mesa/$MESA_ID" | jq '. | length'
echo ""

# 5. Get jurado by ID
echo "[5/10] Get jurado by ID..."
api GET "/api/jurados/$JURADO_ID" | jq '.nombre, .apellido, .rol'
echo ""

# 6. Get jurado by cedula
echo "[6/10] Get jurado by cedula..."
api GET "/api/jurados/cedula/$CEDULA" | jq '.cedula, .nombre'
echo ""

# 7. Submit excusa
echo "[7/10] Submitting excusa..."
EXCUSA=$(api POST "/api/jurados/excusa" "{\"juradoId\":\"$JURADO_ID\",\"mesaId\":\"$MESA_ID\",\"motivo\":\"Enfermedad comprobada\",\"documentoSoporte\":\"cert-medico.pdf\"}")
echo "$EXCUSA" | jq '.id, .estado, .motivo'
EXCUSA_ID=$(echo "$EXCUSA" | jq -r '.id')
echo ""

# 8. Resolve excusa as APROBADA
echo "[8/10] Resolving excusa as APROBADA..."
api POST "/api/jurados/excusa/$EXCUSA_ID/resolver" '{"estado":"APROBADA"}' | jq '.estado, .juradoReemplazoId'
echo ""

# 9. Register asistencia AUSENTE for another jurado
echo "[9/10] Registering asistencia AUSENTE..."
JURADO_ID_2=$(echo "$SORTEO" | jq -r '.mesas[0].jurados[1].id')
api POST "/api/jurados/asistencia" "{\"juradoId\":\"$JURADO_ID_2\",\"mesaId\":\"$MESA_ID\",\"estado\":\"AUSENTE\",\"observacion\":\"No se presento\"}" | jq '.estado, .juradoId'
echo ""

# 10. Final state
echo "[10/10] Final mock state..."
api GET "/mock/state" | jq .
echo ""

echo "[Bonus] Outbox events:"
api GET "/mock/outbox" | jq '. | length'
echo ""

echo "========================================"
echo "All tests completed successfully!"
echo "========================================"
