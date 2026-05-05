package com.selloLegitimo.MockJurados.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class SolicitudSorteoDto {

    @NotBlank(message = "El ID de la eleccion es obligatorio")
    private String eleccionId;

    @NotBlank(message = "El departamento es obligatorio")
    private String departamento;

    @NotBlank(message = "El municipio es obligatorio")
    private String municipio;

    @Min(value = 1, message = "Debe haber al menos 1 mesa")
    private int numeroMesas;

    @Min(value = 1, message = "Debe haber al menos 1 jurado por mesa")
    private int juradosPorMesa;

    private long seed;

    public String getEleccionId() { return eleccionId; }
    public void setEleccionId(String eleccionId) { this.eleccionId = eleccionId; }

    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }

    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }

    public int getNumeroMesas() { return numeroMesas; }
    public void setNumeroMesas(int numeroMesas) { this.numeroMesas = numeroMesas; }

    public int getJuradosPorMesa() { return juradosPorMesa; }
    public void setJuradosPorMesa(int juradosPorMesa) { this.juradosPorMesa = juradosPorMesa; }

    public long getSeed() { return seed; }
    public void setSeed(long seed) { this.seed = seed; }
}
