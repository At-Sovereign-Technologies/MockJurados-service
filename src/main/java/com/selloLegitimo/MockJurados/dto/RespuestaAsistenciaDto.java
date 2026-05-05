package com.selloLegitimo.MockJurados.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;

public class RespuestaAsistenciaDto {

    private String id;
    private String juradoId;
    private String mesaId;
    private String estado;
    private String observacion;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant fechaRegistro;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getJuradoId() { return juradoId; }
    public void setJuradoId(String juradoId) { this.juradoId = juradoId; }

    public String getMesaId() { return mesaId; }
    public void setMesaId(String mesaId) { this.mesaId = mesaId; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }

    public Instant getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(Instant fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
