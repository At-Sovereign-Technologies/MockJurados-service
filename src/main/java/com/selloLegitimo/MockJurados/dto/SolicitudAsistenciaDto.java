package com.selloLegitimo.MockJurados.dto;

import jakarta.validation.constraints.NotBlank;

public class SolicitudAsistenciaDto {

    @NotBlank(message = "El ID del jurado es obligatorio")
    private String juradoId;

    @NotBlank(message = "El ID de la mesa es obligatorio")
    private String mesaId;

    @NotBlank(message = "El estado es obligatorio (PRESENTE / AUSENTE)")
    private String estado;

    private String observacion;

    public String getJuradoId() { return juradoId; }
    public void setJuradoId(String juradoId) { this.juradoId = juradoId; }

    public String getMesaId() { return mesaId; }
    public void setMesaId(String mesaId) { this.mesaId = mesaId; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
}
