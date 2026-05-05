package com.selloLegitimo.MockJurados.modelo;

import java.time.Instant;
import java.util.UUID;

import com.selloLegitimo.MockJurados.modelo.enums.TipoEvento;

public class EventoOutbox {

    private String id;
    private TipoEvento tipo;
    private String payload;
    private Instant fechaEmision;
    private boolean emitido;

    public EventoOutbox() {
        this.id = UUID.randomUUID().toString();
        this.fechaEmision = Instant.now();
        this.emitido = false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public TipoEvento getTipo() { return tipo; }
    public void setTipo(TipoEvento tipo) { this.tipo = tipo; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public Instant getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(Instant fechaEmision) { this.fechaEmision = fechaEmision; }

    public boolean isEmitido() { return emitido; }
    public void setEmitido(boolean emitido) { this.emitido = emitido; }
}
