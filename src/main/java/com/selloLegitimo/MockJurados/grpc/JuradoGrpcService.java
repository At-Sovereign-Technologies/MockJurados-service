package com.selloLegitimo.MockJurados.grpc;

import com.selloLegitimo.MockJurados.dto.EventoOutboxDto;
import com.selloLegitimo.MockJurados.dto.RespuestaAsistenciaDto;
import com.selloLegitimo.MockJurados.dto.RespuestaJuradoDto;
import com.selloLegitimo.MockJurados.servicio.IServicioAsistencia;
import com.selloLegitimo.MockJurados.servicio.IServicioJurados;
import com.selloLegitimo.MockJurados.servicio.ServicioOutbox;
import com.selloLegitimo.grpc.jurados.*;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JuradoGrpcService extends JuradoServiceGrpc.JuradoServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(JuradoGrpcService.class);

    private final IServicioJurados servicioJurados;
    private final IServicioAsistencia servicioAsistencia;
    private final ServicioOutbox servicioOutbox;

    public JuradoGrpcService(IServicioJurados servicioJurados,
                             IServicioAsistencia servicioAsistencia,
                             ServicioOutbox servicioOutbox) {
        this.servicioJurados = servicioJurados;
        this.servicioAsistencia = servicioAsistencia;
        this.servicioOutbox = servicioOutbox;
    }

    @Override
    public void consultarJuradosPorMesa(ConsultarJuradosPorMesaRequest request,
                                        StreamObserver<ConsultarJuradosPorMesaResponse> responseObserver) {
        try {
            String mesaId = request.getMesaId();
            log.info("gRPC: consultarJuradosPorMesa({})", mesaId);

            List<RespuestaJuradoDto> jurados = servicioJurados.buscarPorMesa(mesaId);

            ConsultarJuradosPorMesaResponse.Builder builder = ConsultarJuradosPorMesaResponse.newBuilder();
            for (RespuestaJuradoDto j : jurados) {
                builder.addJurados(toProtoJurado(j));
            }

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("gRPC consultarJuradosPorMesa error", e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void consultarJuradoPorCedula(ConsultarJuradoPorCedulaRequest request,
                                         StreamObserver<JuradoDetalle> responseObserver) {
        try {
            String cedula = request.getCedula();
            log.info("gRPC: consultarJuradoPorCedula({})", cedula);

            RespuestaJuradoDto jurado = servicioJurados.buscarPorCedula(cedula);
            responseObserver.onNext(toProtoJurado(jurado));
            responseObserver.onCompleted();
        } catch (com.selloLegitimo.MockJurados.excepcion.ExcepcionRecursoNoEncontrado e) {
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            log.error("gRPC consultarJuradoPorCedula error", e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void consultarAsistenciaPorMesa(ConsultarAsistenciaPorMesaRequest request,
                                           StreamObserver<ConsultarAsistenciaPorMesaResponse> responseObserver) {
        try {
            String mesaId = request.getMesaId();
            log.info("gRPC: consultarAsistenciaPorMesa({})", mesaId);

            List<RespuestaAsistenciaDto> asistencias = servicioAsistencia.consultarPorMesa(mesaId);

            ConsultarAsistenciaPorMesaResponse.Builder builder =
                    ConsultarAsistenciaPorMesaResponse.newBuilder();
            for (RespuestaAsistenciaDto a : asistencias) {
                builder.addAsistencias(AsistenciaDetalle.newBuilder()
                        .setId(a.getId())
                        .setJuradoId(a.getJuradoId())
                        .setMesaId(a.getMesaId())
                        .setEstado(a.getEstado())
                        .setFechaRegistro(a.getFechaRegistro() != null
                                ? a.getFechaRegistro().toString() : "")
                        .build());
            }

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("gRPC consultarAsistenciaPorMesa error", e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void suscribirEventos(SuscribirEventosRequest request,
                                 StreamObserver<EventoJurado> responseObserver) {
        try {
            log.info("gRPC: suscribirEventos - cliente conectado");

            List<EventoOutboxDto> eventos = servicioOutbox.listarNoEmitidos();
            for (EventoOutboxDto e : eventos) {
                EventoJurado evento = EventoJurado.newBuilder()
                        .setId(e.getId())
                        .setTipo(e.getTipo())
                        .setPayload(e.getPayload())
                        .setFechaEmision(e.getFechaEmision() != null
                                ? e.getFechaEmision().toString() : "")
                        .build();
                responseObserver.onNext(evento);
                servicioOutbox.marcarEmitido(e.getId());
            }

            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("gRPC suscribirEventos error", e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    private JuradoDetalle toProtoJurado(RespuestaJuradoDto j) {
        return JuradoDetalle.newBuilder()
                .setId(j.getId())
                .setCedula(j.getCedula())
                .setNombre(j.getNombre())
                .setApellido(j.getApellido())
                .setMesaId(j.getMesaId())
                .setPuestoId(j.getPuestoId())
                .setRol(j.getRol())
                .setEstado(j.getEstado())
                .build();
    }
}
