package com.selloLegitimo.MockJurados.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;

public class EventoOutboxDto {

    private String id;
    private String tipo;
    private String payload;
    private boolean emitido;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant fechaEmision;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public boolean isEmitido() { return emitido; }
    public void setEmitido(boolean emitido) { this.emitido = emitido; }

    public Instant getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(Instant fechaEmision) { this.fechaEmision = fechaEmision; }
}
