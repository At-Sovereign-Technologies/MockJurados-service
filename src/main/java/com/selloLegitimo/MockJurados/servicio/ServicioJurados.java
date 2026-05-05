package com.selloLegitimo.MockJurados.servicio;

import com.selloLegitimo.MockJurados.modelo.enums.RolJurado;
import com.selloLegitimo.MockJurados.util.GeneradorDeterministico;
import com.selloLegitimo.MockJurados.dto.*;
import com.selloLegitimo.MockJurados.excepcion.ExcepcionReglaNegocio;
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

    public ServicioJurados(RepositorioJurado repositorioJurado,
                           RepositorioMesa repositorioMesa,
                           GeneradorDeterministico generador,
                           ServicioOutbox servicioOutbox) {
        this.repositorioJurado = repositorioJurado;
        this.repositorioMesa = repositorioMesa;
        this.generador = generador;
        this.servicioOutbox = servicioOutbox;
    }

    @Override
    public RespuestaSorteoDto realizarSorteo(SolicitudSorteoDto solicitud) {
        int juradosPorMesa = solicitud.getJuradosPorMesa() > 0
                ? solicitud.getJuradosPorMesa()
                : JURADOS_POR_MESA_POR_DEFECTO;

        log.info("Iniciando sorteo: eleccion={}, depto={}, municipio={}, mesas={}, seed={}",
                solicitud.getEleccionId(), solicitud.getDepartamento(),
                solicitud.getMunicipio(), solicitud.getNumeroMesas(), solicitud.getSeed());

        Random rng = generador.crearRandom(
                solicitud.getSeed(),
                solicitud.getEleccionId(),
                solicitud.getDepartamento(),
                solicitud.getMunicipio()
        );

        Set<String> cedulasUsadas = new HashSet<>();
        List<RespuestaSorteoDto.RespuestaMesaDto> mesasDto = new ArrayList<>();

        for (int i = 1; i <= solicitud.getNumeroMesas(); i++) {
            String mesaId = UUID.randomUUID().toString();
            String puestoId = "PUESTO-" + solicitud.getDepartamento().substring(0, 3).toUpperCase() + "-001";

            Mesa mesa = new Mesa();
            mesa.setId(mesaId);
            mesa.setNumero(i);
            mesa.setPuestoId(puestoId);
            mesa.setDepartamento(solicitud.getDepartamento());
            mesa.setMunicipio(solicitud.getMunicipio());
            repositorioMesa.guardar(mesa);

            List<RespuestaJuradoDto> juradosDto = new ArrayList<>();

            for (int r = 0; r < juradosPorMesa; r++) {
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
                jurado.setRol(ROLES.get(r % ROLES.size()));
                repositorioJurado.guardar(jurado);

                mesa.getJuradoIds().add(jurado.getId());

                RespuestaJuradoDto jDto = toJuradoDto(jurado);
                juradosDto.add(jDto);

                servicioOutbox.emitir("JURADO_ASIGNADO", Map.of(
                    "juradoId", jurado.getId(),
                    "cedula", jurado.getCedula(),
                    "nombre", jurado.getNombre() + " " + jurado.getApellido(),
                    "mesaId", mesaId,
                    "rol", jurado.getRol().name(),
                    "puestoId", puestoId
                ));
            }

            RespuestaSorteoDto.RespuestaMesaDto mDto = new RespuestaSorteoDto.RespuestaMesaDto();
            mDto.setId(mesaId);
            mDto.setNumero(i);
            mDto.setJurados(juradosDto);
            mesasDto.add(mDto);
        }

        RespuestaSorteoDto respuesta = new RespuestaSorteoDto();
        respuesta.setEleccionId(solicitud.getEleccionId());
        respuesta.setDepartamento(solicitud.getDepartamento());
        respuesta.setMunicipio(solicitud.getMunicipio());
        respuesta.setSeed(solicitud.getSeed());
        respuesta.setTotalMesas(solicitud.getNumeroMesas());
        respuesta.setTotalJurados((int) repositorioJurado.contar());
        respuesta.setMesas(mesasDto);

        log.info("Sorteo completado: {} jurados asignados a {} mesas",
                respuesta.getTotalJurados(), respuesta.getTotalMesas());

        return respuesta;
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
        dto.setFechaCreacion(j.getFechaCreacion());
        return dto;
    }
}
