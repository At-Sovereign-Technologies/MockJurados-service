package com.selloLegitimo.MockJurados.servicio;

import com.selloLegitimo.MockJurados.client.ClienteCensoElectoral;
import com.selloLegitimo.MockJurados.client.ClienteCensoElectoral.CiudadanoCensoDto;
import com.selloLegitimo.MockJurados.modelo.enums.RolJurado;
import com.selloLegitimo.MockJurados.util.GeneradorDeterministico;
import com.selloLegitimo.MockJurados.grpc.GrpcConfiguracionClient;
import com.selloLegitimo.grpc.elecciones.EleccionDetalle;
import com.selloLegitimo.MockJurados.dto.*;
import com.selloLegitimo.MockJurados.modelo.Jurado;
import com.selloLegitimo.MockJurados.modelo.Mesa;
import com.selloLegitimo.MockJurados.repositorio.RepositorioJurado;
import com.selloLegitimo.MockJurados.repositorio.RepositorioMesa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ServicioJurados implements IServicioJurados {

    private static final Logger log = LoggerFactory.getLogger(ServicioJurados.class);

    private static final int JURADOS_POR_MESA_POR_DEFECTO = 4;

    private static final List<RolJurado> ROLES = List.of(
        RolJurado.PRESIDENTE,
        RolJurado.SECRETARIO,
        RolJurado.VOCAL_1,
        RolJurado.VOCAL_2
    );

    private final RepositorioJurado repositorioJurado;
    private final RepositorioMesa repositorioMesa;
    private final GeneradorDeterministico generador;
    private final ServicioOutbox servicioOutbox;
    private final ClienteCensoElectoral clienteCenso;
    private final GrpcConfiguracionClient grpcConfiguracionClient;

    public ServicioJurados(RepositorioJurado repositorioJurado,
                           RepositorioMesa repositorioMesa,
                           GeneradorDeterministico generador,
                           ServicioOutbox servicioOutbox,
                           ClienteCensoElectoral clienteCenso,
                           GrpcConfiguracionClient grpcConfiguracionClient) {
        this.repositorioJurado = repositorioJurado;
        this.repositorioMesa = repositorioMesa;
        this.generador = generador;
        this.servicioOutbox = servicioOutbox;
        this.clienteCenso = clienteCenso;
        this.grpcConfiguracionClient = grpcConfiguracionClient;
    }

    @Override
    public RespuestaSorteoDto realizarSorteo(SolicitudSorteoDto solicitud) {
        Long eleccionId = solicitud.getEleccionId();

        EleccionDetalle eleccionValidada = grpcConfiguracionClient.validarEleccion(eleccionId);
        if (eleccionValidada == null) {
            throw new com.selloLegitimo.MockJurados.excepcion.ExcepcionReglaNegocio(
                    "La eleccion con ID " + eleccionId + " no existe o no pudo ser validada");
        }

        int juradosPorMesa = solicitud.getJuradosPorMesa() > 0
                ? solicitud.getJuradosPorMesa()
                : JURADOS_POR_MESA_POR_DEFECTO;

        log.info("Iniciando sorteo: eleccion={} ({}) depto={}, municipio={}, mesas={}, seed={}",
                eleccionId, eleccionValidada.getNombreOficial(), solicitud.getDepartamento(),
                solicitud.getMunicipio(), solicitud.getNumeroMesas(), solicitud.getSeed());

        List<CiudadanoCensoDto> ciudadanosCenso = List.of();
        boolean usarCenso = false;

        if (clienteCenso.isDisponible()) {
            try {
                ciudadanosCenso = clienteCenso.obtenerCiudadanosHabilitados(
                        eleccionId, solicitud.getDepartamento(), solicitud.getMunicipio());

                List<String> candidatosEnEleccion = clienteCenso.obtenerCandidatosEnEleccion(eleccionId);
                Set<String> candidatosSet = new java.util.HashSet<>(candidatosEnEleccion);

                ciudadanosCenso = ciudadanosCenso.stream()
                        .filter(c -> !candidatosSet.contains(c.numeroDocumento))
                        .toList();

                if (!ciudadanosCenso.isEmpty()) {
                    usarCenso = true;
                    log.info("Sorteo usara {} ciudadanos del censo electoral para eleccion={} (excluidos {} candidatos)",
                            ciudadanosCenso.size(), eleccionId, candidatosSet.size());
                }
            } catch (Exception ex) {
                log.warn("Error consultando censo electoral, usando generador deterministico: {}", ex.getMessage());
            }
        }

        if (!usarCenso) {
            log.info("Censo electoral no disponible o vacio, usando generador deterministico");
        }

        Random rng = generador.crearRandom(
                solicitud.getSeed(),
                String.valueOf(eleccionId),
                solicitud.getDepartamento(),
                solicitud.getMunicipio()
        );

        Set<String> cedulasUsadas = new HashSet<>();
        List<RespuestaSorteoDto.RespuestaMesaDto> mesasDto = new ArrayList<>();

        if (usarCenso) {
            List<CiudadanoCensoDto> poolCenso = new ArrayList<>(ciudadanosCenso);
            Collections.shuffle(poolCenso, rng);

            int indiceCenso = 0;

            for (int i = 1; i <= solicitud.getNumeroMesas(); i++) {
                String mesaId = UUID.randomUUID().toString();
                String puestoId = "PUESTO-" + solicitud.getDepartamento().substring(0, Math.min(3, solicitud.getDepartamento().length())).toUpperCase() + "-001";

                Mesa mesa = new Mesa();
                mesa.setId(mesaId);
                mesa.setNumero(i);
                mesa.setPuestoId(puestoId);
                mesa.setDepartamento(solicitud.getDepartamento());
                mesa.setMunicipio(solicitud.getMunicipio());
                repositorioMesa.guardar(mesa);

                List<RespuestaJuradoDto> juradosDto = new ArrayList<>();

                for (int r = 0; r < juradosPorMesa; r++) {
                    CiudadanoCensoDto ciudadano;
                    if (indiceCenso < poolCenso.size()) {
                        ciudadano = poolCenso.get(indiceCenso);
                        indiceCenso++;
                    } else {
                        int idx = rng.nextInt(poolCenso.size());
                        ciudadano = poolCenso.get(idx);
                    }

                    if (cedulasUsadas.contains(ciudadano.numeroDocumento)) {
                        Jurado juradoFallback = generarJuradoSintetico(rng, mesaId, puestoId, cedulasUsadas, eleccionId);
                        juradosDto.add(toJuradoDto(juradoFallback));
                        continue;
                    }
                    cedulasUsadas.add(ciudadano.numeroDocumento);

                    String nombreCompleto = ciudadano.nombres;
                    String[] partesNombre = nombreCompleto.split(" ", 2);
                    String primerNombre = partesNombre[0];
                    String apellidos = ciudadano.apellidos;

                    Jurado jurado = new Jurado();
                    jurado.setCedula(ciudadano.numeroDocumento);
                    jurado.setNombre(primerNombre);
                    jurado.setApellido(apellidos);
                    jurado.setMesaId(mesaId);
                    jurado.setPuestoId(puestoId);
                    jurado.setRol(ROLES.get(r % ROLES.size()));
                    jurado.setEleccionId(eleccionId);
                    repositorioJurado.guardar(jurado);

                    mesa.getJuradoIds().add(jurado.getId());
                    juradosDto.add(toJuradoDto(jurado));

                    servicioOutbox.emitir("JURADO_ASIGNADO", Map.of(
                        "juradoId", jurado.getId(),
                        "cedula", jurado.getCedula(),
                        "nombre", jurado.getNombre() + " " + jurado.getApellido(),
                        "mesaId", mesaId,
                        "rol", jurado.getRol().name(),
                        "puestoId", puestoId,
                        "eleccionId", String.valueOf(eleccionId),
                        "fuente", "CENSO_ELECTORAL"
                    ));
                }

                RespuestaSorteoDto.RespuestaMesaDto mDto = new RespuestaSorteoDto.RespuestaMesaDto();
                mDto.setId(mesaId);
                mDto.setNumero(i);
                mDto.setJurados(juradosDto);
                mesasDto.add(mDto);
            }
        } else {
            for (int i = 1; i <= solicitud.getNumeroMesas(); i++) {
                String mesaId = UUID.randomUUID().toString();
                String puestoId = "PUESTO-" + solicitud.getDepartamento().substring(0, Math.min(3, solicitud.getDepartamento().length())).toUpperCase() + "-001";

                Mesa mesa = new Mesa();
                mesa.setId(mesaId);
                mesa.setNumero(i);
                mesa.setPuestoId(puestoId);
                mesa.setDepartamento(solicitud.getDepartamento());
                mesa.setMunicipio(solicitud.getMunicipio());
                repositorioMesa.guardar(mesa);

                List<RespuestaJuradoDto> juradosDto = new ArrayList<>();

                for (int r = 0; r < juradosPorMesa; r++) {
                    Jurado jurado = generarJuradoSintetico(rng, mesaId, puestoId, cedulasUsadas, eleccionId);
                    jurado.setRol(ROLES.get(r % ROLES.size()));
                    juradosDto.add(toJuradoDto(jurado));

                    servicioOutbox.emitir("JURADO_ASIGNADO", Map.of(
                        "juradoId", jurado.getId(),
                        "cedula", jurado.getCedula(),
                        "nombre", jurado.getNombre() + " " + jurado.getApellido(),
                        "mesaId", mesaId,
                        "rol", jurado.getRol().name(),
                        "puestoId", puestoId,
                        "eleccionId", String.valueOf(eleccionId),
                        "fuente", "GENERADOR_DETERMINISTICO"
                    ));
                }

                RespuestaSorteoDto.RespuestaMesaDto mDto = new RespuestaSorteoDto.RespuestaMesaDto();
                mDto.setId(mesaId);
                mDto.setNumero(i);
                mDto.setJurados(juradosDto);
                mesasDto.add(mDto);
            }
        }

        RespuestaSorteoDto respuesta = new RespuestaSorteoDto();
        respuesta.setEleccionId(eleccionId);
        respuesta.setDepartamento(solicitud.getDepartamento());
        respuesta.setMunicipio(solicitud.getMunicipio());
        respuesta.setSeed(solicitud.getSeed());
        respuesta.setTotalMesas(solicitud.getNumeroMesas());
        respuesta.setTotalJurados((int) repositorioJurado.contar());
        respuesta.setMesas(mesasDto);

        log.info("Sorteo completado: {} jurados asignados a {} mesas (fuente={})",
                respuesta.getTotalJurados(), respuesta.getTotalMesas(),
                usarCenso ? "CENSO_ELECTORAL" : "GENERADOR_DETERMINISTICO");

        return respuesta;
    }

    private Jurado generarJuradoSintetico(Random rng, String mesaId, String puestoId, Set<String> cedulasUsadas, Long eleccionId) {
        String cedula;
        do {
            cedula = generador.generarCedula(rng);
        } while (cedulasUsadas.contains(cedula));
        cedulasUsadas.add(cedula);

        Jurado jurado = new Jurado();
        jurado.setCedula(cedula);
        jurado.setNombre(generador.generarNombre(rng));
        jurado.setApellido(generador.generarApellido(rng));
        jurado.setMesaId(mesaId);
        jurado.setPuestoId(puestoId);
        jurado.setEleccionId(eleccionId);
        repositorioJurado.guardar(jurado);

        Mesa mesa = repositorioMesa.buscarPorId(mesaId);
        if (mesa != null) {
            mesa.getJuradoIds().add(jurado.getId());
        }

        return jurado;
    }

    @Override
    public RespuestaJuradoDto buscarPorId(String id) {
        Jurado jurado = repositorioJurado.buscarPorId(id);
        if (jurado == null) {
            throw new com.selloLegitimo.MockJurados.excepcion.ExcepcionRecursoNoEncontrado("Jurado", id);
        }
        return toJuradoDto(jurado);
    }

    @Override
    public RespuestaJuradoDto buscarPorCedula(String cedula) {
        List<Jurado> resultados = repositorioJurado.buscarPorCedula(cedula);
        if (resultados.isEmpty()) {
            throw new com.selloLegitimo.MockJurados.excepcion.ExcepcionRecursoNoEncontrado("Jurado con cedula", cedula);
        }
        return toJuradoDto(resultados.get(0));
    }

    @Override
    public List<RespuestaJuradoDto> buscarPorMesa(String mesaId) {
        return repositorioJurado.buscarPorMesa(mesaId)
                .stream()
                .map(this::toJuradoDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RespuestaJuradoDto> listarTodos() {
        return repositorioJurado.buscarTodos()
                .stream()
                .map(this::toJuradoDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RespuestaJuradoDto> listarPorEleccion(Long eleccionId) {
        return repositorioJurado.buscarPorEleccion(eleccionId)
                .stream()
                .map(this::toJuradoDto)
                .collect(Collectors.toList());
    }

    @Override
    public void limpiar() {
        repositorioJurado.limpiar();
        repositorioMesa.limpiar();
        log.info("Datos de jurados y mesas limpiados");
    }

    Jurado generarReemplazo(Jurado original) {
        Random rng = generador.crearRandom(
                System.currentTimeMillis(),
                original.getId(),
                original.getMesaId(),
                original.getRol().name()
        );

        String cedula;
        do {
            cedula = generador.generarCedula(rng);
        } while (repositorioJurado.existeCedulaEnSorteo(cedula));

        Jurado reemplazo = new Jurado();
        reemplazo.setCedula(cedula);
        reemplazo.setNombre(generador.generarNombre(rng));
        reemplazo.setApellido(generador.generarApellido(rng));
        reemplazo.setMesaId(original.getMesaId());
        reemplazo.setPuestoId(original.getPuestoId());
        reemplazo.setRol(original.getRol());
        reemplazo.setReemplazaA(original.getId());
        reemplazo.setEleccionId(original.getEleccionId());
        repositorioJurado.guardar(reemplazo);

        Mesa mesa = repositorioMesa.buscarPorId(original.getMesaId());
        if (mesa != null) {
            mesa.getJuradoIds().add(reemplazo.getId());
        }

        return reemplazo;
    }

    private RespuestaJuradoDto toJuradoDto(Jurado j) {
        RespuestaJuradoDto dto = new RespuestaJuradoDto();
        dto.setId(j.getId());
        dto.setCedula(j.getCedula());
        dto.setNombre(j.getNombre());
        dto.setApellido(j.getApellido());
        dto.setMesaId(j.getMesaId());
        dto.setPuestoId(j.getPuestoId());
        dto.setRol(j.getRol().name());
        dto.setEstado(j.getEstado().name());
        dto.setReemplazaA(j.getReemplazaA());
        dto.setEleccionId(j.getEleccionId());
        dto.setFechaCreacion(j.getFechaCreacion());
        return dto;
    }
}