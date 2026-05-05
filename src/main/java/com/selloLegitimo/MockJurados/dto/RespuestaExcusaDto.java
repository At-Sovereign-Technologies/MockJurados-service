package com.selloLegitimo.MockJurados.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;

public class RespuestaExcusaDto {

    private String id;
    private String juradoId;
    private String mesaId;
    private String motivo;
    private String documentoSoporte;
    private String estado;
    private String juradoReemplazoId;
    private String motivoRechazo;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant fechaSolicitud;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant fechaResolucion;

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

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getJuradoReemplazoId() { return juradoReemplazoId; }
    public void setJuradoReemplazoId(String juradoReemplazoId) { this.juradoReemplazoId = juradoReemplazoId; }

    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }

    public Instant getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(Instant fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }

    public Instant getFechaResolucion() { return fechaResolucion; }
    public void setFechaResolucion(Instant fechaResolucion) { this.fechaResolucion = fechaResolucion; }
}
