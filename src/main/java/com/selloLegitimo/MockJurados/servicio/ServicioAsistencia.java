package com.selloLegitimo.MockJurados.servicio;

import com.selloLegitimo.MockJurados.dto.*;
import com.selloLegitimo.MockJurados.excepcion.ExcepcionRecursoNoEncontrado;
import com.selloLegitimo.MockJurados.excepcion.ExcepcionReglaNegocio;
import com.selloLegitimo.MockJurados.modelo.Asistencia;
import com.selloLegitimo.MockJurados.modelo.Jurado;
import com.selloLegitimo.MockJurados.modelo.enums.EstadoAsistencia;
import com.selloLegitimo.MockJurados.modelo.enums.EstadoJurado;
import com.selloLegitimo.MockJurados.repositorio.RepositorioAsistencia;
import com.selloLegitimo.MockJurados.repositorio.RepositorioJurado;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ServicioAsistencia implements IServicioAsistencia {

    private static final Logger log = LoggerFactory.getLogger(ServicioAsistencia.class);

    private final RepositorioAsistencia repositorioAsistencia;
    private final RepositorioJurado repositorioJurado;
    private final ServicioJurados servicioJurados;
    private final ServicioOutbox servicioOutbox;

    public ServicioAsistencia(RepositorioAsistencia repositorioAsistencia,
                              RepositorioJurado repositorioJurado,
                              ServicioJurados servicioJurados,
                              ServicioOutbox servicioOutbox) {
        this.repositorioAsistencia = repositorioAsistencia;
        this.repositorioJurado = repositorioJurado;
        this.servicioJurados = servicioJurados;
        this.servicioOutbox = servicioOutbox;
    }

    @Override
    public RespuestaAsistenciaDto registrarAsistencia(SolicitudAsistenciaDto solicitud) {
        Jurado jurado = repositorioJurado.buscarPorId(solicitud.getJuradoId());
        if (jurado == null) {
            throw new ExcepcionRecursoNoEncontrado("Jurado", solicitud.getJuradoId());
        }

        EstadoAsistencia estado;
        try {
            estado = EstadoAsistencia.valueOf(solicitud.getEstado());
        } catch (IllegalArgumentException e) {
            throw new ExcepcionReglaNegocio("Estado invalido: " + solicitud.getEstado()
                    + ". Valores permitidos: PRESENTE, AUSENTE");
        }

        Asistencia asistencia = new Asistencia();
        asistencia.setJuradoId(solicitud.getJuradoId());
        asistencia.setMesaId(solicitud.getMesaId());
        asistencia.setEstado(estado);
        asistencia.setObservacion(solicitud.getObservacion());
        repositorioAsistencia.guardar(asistencia);

        log.info("Asistencia registrada: jurado={}, estado={}", solicitud.getJuradoId(), estado);

        servicioOutbox.emitir("ASISTENCIA_REGISTRADA", Map.of(
            "asistenciaId", asistencia.getId(),
            "juradoId", asistencia.getJuradoId(),
            "estado", asistencia.getEstado().name(),
            "mesaId", asistencia.getMesaId()
        ));

        if (estado == EstadoAsistencia.AUSENTE) {
            Jurado reemplazo = servicioJurados.generarReemplazo(jurado);
            jurado.setEstado(EstadoJurado.REEMPLAZADO);
            repositorioJurado.guardar(jurado);

            log.info("AUSENTE -> reemplazo generado: jurado={}, reemplazo={}",
                    jurado.getId(), reemplazo.getId());

            servicioOutbox.emitir("REEMPLAZO_GENERADO", Map.of(
                "juradoReemplazoId", reemplazo.getId(),
                "cedula", reemplazo.getCedula(),
                "nombre", reemplazo.getNombre() + " " + reemplazo.getApellido(),
                "mesaId", reemplazo.getMesaId(),
                "rol", reemplazo.getRol().name(),
                "reemplazaA", reemplazo.getReemplazaA()
            ));
        }

        return toAsistenciaDto(asistencia);
    }

    @Override
    public List<RespuestaAsistenciaDto> consultarPorMesa(String mesaId) {
        return repositorioAsistencia.buscarPorMesa(mesaId)
                .stream()
                .map(this::toAsistenciaDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RespuestaAsistenciaDto> listarTodos() {
        return repositorioAsistencia.buscarTodos()
                .stream()
                .map(this::toAsistenciaDto)
                .collect(Collectors.toList());
    }

    private RespuestaAsistenciaDto toAsistenciaDto(Asistencia a) {
        RespuestaAsistenciaDto dto = new RespuestaAsistenciaDto();
        dto.setId(a.getId());
        dto.setJuradoId(a.getJuradoId());
        dto.setMesaId(a.getMesaId());
        dto.setEstado(a.getEstado().name());
        dto.setObservacion(a.getObservacion());
        dto.setFechaRegistro(a.getFechaRegistro());
        return dto;
    }
}
