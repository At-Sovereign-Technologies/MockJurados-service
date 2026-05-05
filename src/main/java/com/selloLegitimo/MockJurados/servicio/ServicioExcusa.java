package com.selloLegitimo.MockJurados.servicio;

import com.selloLegitimo.MockJurados.dto.*;
import com.selloLegitimo.MockJurados.excepcion.ExcepcionRecursoNoEncontrado;
import com.selloLegitimo.MockJurados.excepcion.ExcepcionReglaNegocio;
import com.selloLegitimo.MockJurados.modelo.Excusa;
import com.selloLegitimo.MockJurados.modelo.Jurado;
import com.selloLegitimo.MockJurados.modelo.enums.EstadoExcusa;
import com.selloLegitimo.MockJurados.modelo.enums.EstadoJurado;
import com.selloLegitimo.MockJurados.repositorio.RepositorioExcusa;
import com.selloLegitimo.MockJurados.repositorio.RepositorioJurado;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ServicioExcusa implements IServicioExcusa {

    private static final Logger log = LoggerFactory.getLogger(ServicioExcusa.class);

    private final RepositorioExcusa repositorioExcusa;
    private final RepositorioJurado repositorioJurado;
    private final ServicioJurados servicioJurados;
    private final ServicioOutbox servicioOutbox;

    public ServicioExcusa(RepositorioExcusa repositorioExcusa,
                          RepositorioJurado repositorioJurado,
                          ServicioJurados servicioJurados,
                          ServicioOutbox servicioOutbox) {
        this.repositorioExcusa = repositorioExcusa;
        this.repositorioJurado = repositorioJurado;
        this.servicioJurados = servicioJurados;
        this.servicioOutbox = servicioOutbox;
    }

    @Override
    public RespuestaExcusaDto presentarExcusa(SolicitudExcusaDto solicitud) {
        Jurado jurado = repositorioJurado.buscarPorId(solicitud.getJuradoId());
        if (jurado == null) {
            throw new ExcepcionRecursoNoEncontrado("Jurado", solicitud.getJuradoId());
        }

        if (jurado.getEstado() == EstadoJurado.REEMPLAZADO) {
            throw new ExcepcionReglaNegocio("El jurado ya ha sido reemplazado. No se pueden presentar mas excusas.");
        }

        if (jurado.getEstado() == EstadoJurado.EXCUSADO) {
            List<Excusa> existentes = repositorioExcusa.buscarPorJurado(jurado.getId());
            boolean pendiente = existentes.stream()
                    .anyMatch(e -> e.getEstado() == EstadoExcusa.PENDIENTE);
            if (pendiente) {
                throw new ExcepcionReglaNegocio("El jurado ya tiene una excusa pendiente.");
            }
        }

        Excusa excusa = new Excusa();
        excusa.setJuradoId(solicitud.getJuradoId());
        excusa.setMesaId(solicitud.getMesaId());
        excusa.setMotivo(solicitud.getMotivo());
        excusa.setDocumentoSoporte(solicitud.getDocumentoSoporte());
        repositorioExcusa.guardar(excusa);

        jurado.setEstado(EstadoJurado.EXCUSADO);
        repositorioJurado.guardar(jurado);

        log.info("Excusa presentada: id={}, jurado={}, motivo={}",
                excusa.getId(), jurado.getId(), solicitud.getMotivo());

        return toExcusaDto(excusa);
    }

    @Override
    public RespuestaExcusaDto resolverExcusa(String excusaId, SolicitudResolverExcusaDto resolucion) {
        Excusa excusa = repositorioExcusa.buscarPorId(excusaId);
        if (excusa == null) {
            throw new ExcepcionRecursoNoEncontrado("Excusa", excusaId);
        }

        if (excusa.getEstado() != EstadoExcusa.PENDIENTE) {
            throw new ExcepcionReglaNegocio("La excusa ya fue resuelta: " + excusa.getEstado());
        }

        EstadoExcusa nuevoEstado;
        try {
            nuevoEstado = EstadoExcusa.valueOf(resolucion.getEstado());
        } catch (IllegalArgumentException e) {
            throw new ExcepcionReglaNegocio("Estado invalido: " + resolucion.getEstado()
                    + ". Valores permitidos: APROBADA, RECHAZADA");
        }

        excusa.setEstado(nuevoEstado);
        excusa.setFechaResolucion(Instant.now());

        Jurado jurado = repositorioJurado.buscarPorId(excusa.getJuradoId());

        if (nuevoEstado == EstadoExcusa.APROBADA) {
            if (jurado != null) {
                jurado.setEstado(EstadoJurado.REEMPLAZADO);
                repositorioJurado.guardar(jurado);

                Jurado reemplazo = servicioJurados.generarReemplazo(jurado);
                excusa.setJuradoReemplazoId(reemplazo.getId());

                log.info("Excusa APROBADA: excusa={}, reemplazo={}", excusaId, reemplazo.getId());

                servicioOutbox.emitir("EXCUSA_RESUELTA", Map.of(
                    "excusaId", excusaId,
                    "resolucion", "APROBADA",
                    "juradoOriginal", jurado.getId(),
                    "juradoReemplazo", reemplazo.getId()
                ));

                servicioOutbox.emitir("REEMPLAZO_GENERADO", Map.of(
                    "juradoReemplazoId", reemplazo.getId(),
                    "cedula", reemplazo.getCedula(),
                    "nombre", reemplazo.getNombre() + " " + reemplazo.getApellido(),
                    "mesaId", reemplazo.getMesaId(),
                    "rol", reemplazo.getRol().name(),
                    "reemplazaA", reemplazo.getReemplazaA()
                ));
            }
        } else {
            excusa.setMotivoRechazo(resolucion.getMotivoRechazo());
            if (jurado != null) {
                jurado.setEstado(EstadoJurado.ASIGNADO);
                repositorioJurado.guardar(jurado);
            }
            log.info("Excusa RECHAZADA: excusa={}, motivo={}", excusaId, resolucion.getMotivoRechazo());

            servicioOutbox.emitir("EXCUSA_RESUELTA", Map.of(
                "excusaId", excusaId,
                "resolucion", "RECHAZADA",
                "motivo", resolucion.getMotivoRechazo() != null ? resolucion.getMotivoRechazo() : ""
            ));
        }

        repositorioExcusa.guardar(excusa);
        return toExcusaDto(excusa);
    }

    @Override
    public RespuestaExcusaDto buscarPorId(String id) {
        Excusa excusa = repositorioExcusa.buscarPorId(id);
        if (excusa == null) {
            throw new ExcepcionRecursoNoEncontrado("Excusa", id);
        }
        return toExcusaDto(excusa);
    }

    @Override
    public List<RespuestaExcusaDto> listarTodos() {
        return repositorioExcusa.buscarTodos()
                .stream()
                .map(this::toExcusaDto)
                .collect(Collectors.toList());
    }

    private RespuestaExcusaDto toExcusaDto(Excusa e) {
        RespuestaExcusaDto dto = new RespuestaExcusaDto();
        dto.setId(e.getId());
        dto.setJuradoId(e.getJuradoId());
        dto.setMesaId(e.getMesaId());
        dto.setMotivo(e.getMotivo());
        dto.setDocumentoSoporte(e.getDocumentoSoporte());
        dto.setEstado(e.getEstado().name());
        dto.setJuradoReemplazoId(e.getJuradoReemplazoId());
        dto.setMotivoRechazo(e.getMotivoRechazo());
        dto.setFechaSolicitud(e.getFechaSolicitud());
        dto.setFechaResolucion(e.getFechaResolucion());
        return dto;
    }
}
