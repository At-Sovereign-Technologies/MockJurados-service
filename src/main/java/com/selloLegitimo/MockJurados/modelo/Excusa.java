package com.selloLegitimo.MockJurados.modelo;

import java.time.Instant;
import java.util.UUID;

import com.selloLegitimo.MockJurados.modelo.enums.EstadoExcusa;

public class Excusa {

    private String id;
    private String juradoId;
    private String mesaId;
    private String motivo;
    private String documentoSoporte;
    private EstadoExcusa estado;
    private Instant fechaSolicitud;
    private Instant fechaResolucion;
    private String juradoReemplazoId;
    private String motivoRechazo;

    public Excusa() {
        this.id = UUID.randomUUID().toString();
        this.estado = EstadoExcusa.PENDIENTE;
        this.fechaSolicitud = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getJuradoId() { return juradoId; }
    public void setJuradoId(String juradoId) { this.juradoId = juradoId; }

    public String getMesaId() { return mesaId; }
    public void setMesaId(String mesaId) { this.mesaId = mesaId; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getDocumentoSoporte() { return documentoSoporte; }
    public void setDocumentoSoporte(String documentoSoporte) { this.documentoSoporte = documentoSoporte; }

    public EstadoExcusa getEstado() { return estado; }
    public void setEstado(EstadoExcusa estado) { this.estado = estado; }

    public Instant getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(Instant fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }

    public Instant getFechaResolucion() { return fechaResolucion; }
    public void setFechaResolucion(Instant fechaResolucion) { this.fechaResolucion = fechaResolucion; }

    public String getJuradoReemplazoId() { return juradoReemplazoId; }
    public void setJuradoReemplazoId(String juradoReemplazoId) { this.juradoReemplazoId = juradoReemplazoId; }

    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }
}
