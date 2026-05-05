package com.selloLegitimo.MockJurados.grpc;

import com.selloLegitimo.grpc.elecciones.EleccionServiceGrpc;
import com.selloLegitimo.grpc.elecciones.ObtenerEleccionRequest;
import com.selloLegitimo.grpc.elecciones.EleccionDetalle;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import jakarta.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcConfiguracionClient implements DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(GrpcConfiguracionClient.class);

    @Value("${grpc.elecciones.host}")
    private String host;

    @Value("${grpc.elecciones.port}")
    private int port;

    private ManagedChannel channel;

    @Bean
    public EleccionServiceGrpc.EleccionServiceBlockingStub eleccionServiceStub() {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        log.info("Canal gRPC hacia ConfiguracionEleccion creado en {}:{}", host, port);
        return EleccionServiceGrpc.newBlockingStub(channel);
    }

    public EleccionDetalle validarEleccion(Long eleccionId) {
        EleccionServiceGrpc.EleccionServiceBlockingStub stub = eleccionServiceStub();
        try {
            ObtenerEleccionRequest request = ObtenerEleccionRequest.newBuilder()
                    .setId(eleccionId)
                    .build();
            EleccionDetalle detalle = stub.obtenerEleccion(request);
            log.info("Eleccion validada: id={}, nombre={}", detalle.getId(), detalle.getNombreOficial());
            return detalle;
        } catch (StatusRuntimeException e) {
            log.warn("No se pudo validar la eleccion {} via gRPC: {}", eleccionId, e.getStatus());
            return null;
        }
    }

    @Override
    public void destroy() {
        if (channel != null && !channel.isShutdown()) {
            log.info("Cerrando canal gRPC hacia ConfiguracionEleccion...");
            channel.shutdown();
        }
    }
}
