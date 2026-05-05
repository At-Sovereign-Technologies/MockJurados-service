package com.selloLegitimo.MockJurados.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GrpcServerConfig {

    private static final Logger log = LoggerFactory.getLogger(GrpcServerConfig.class);

    @Value("${grpc.server.port:9091}")
    private int grpcPort;

    private final JuradoGrpcService juradoGrpcService;
    private Server server;

    public GrpcServerConfig(JuradoGrpcService juradoGrpcService) {
        this.juradoGrpcService = juradoGrpcService;
    }

    @PostConstruct
    public void start() throws Exception {
        server = ServerBuilder.forPort(grpcPort)
                .addService(juradoGrpcService)
                .build()
                .start();
        log.info("Servidor gRPC de Jurados iniciado en puerto {}", grpcPort);
    }

    @PreDestroy
    public void stop() {
        if (server != null) {
            log.info("Deteniendo servidor gRPC de Jurados...");
            server.shutdown();
        }
    }
}
