package com.selloLegitimo.MockJurados.dto;

import jakarta.validation.constraints.NotBlank;

public class SolicitudResolverExcusaDto {

    @NotBlank(message = "El estado de resolucion es obligatorio (APROBADA / RECHAZADA)")
    private String estado;

    private String motivoRechazo;

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }
}
