package com.selloLegitimo.MockJurados.dto;

import jakarta.validation.constraints.NotBlank;

public class SolicitudExcusaDto {

    @NotBlank(message = "El ID del jurado es obligatorio")
    private String juradoId;

    @NotBlank(message = "El ID de la mesa es obligatorio")
    private String mesaId;

    @NotBlank(message = "El motivo es obligatorio")
    private String motivo;

    private String documentoSoporte;

    public String getJuradoId() { return juradoId; }
    public void setJuradoId(String juradoId) { this.juradoId = juradoId; }

    public String getMesaId() { return mesaId; }
    public void setMesaId(String mesaId) { this.mesaId = mesaId; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getDocumentoSoporte() { return documentoSoporte; }
    public void setDocumentoSoporte(String documentoSoporte) { this.documentoSoporte = documentoSoporte; }
}
