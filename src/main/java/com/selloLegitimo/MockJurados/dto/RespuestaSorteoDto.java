package com.selloLegitimo.MockJurados.dto;

import java.util.List;

public class RespuestaSorteoDto {

    private Long eleccionId;
    private String departamento;
    private String municipio;
    private long seed;
    private int totalMesas;
    private int totalJurados;
    private List<RespuestaMesaDto> mesas;

    public Long getEleccionId() { return eleccionId; }
    public void setEleccionId(Long eleccionId) { this.eleccionId = eleccionId; }

    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }

    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }

    public long getSeed() { return seed; }
    public void setSeed(long seed) { this.seed = seed; }

    public int getTotalMesas() { return totalMesas; }
    public void setTotalMesas(int totalMesas) { this.totalMesas = totalMesas; }

    public int getTotalJurados() { return totalJurados; }
    public void setTotalJurados(int totalJurados) { this.totalJurados = totalJurados; }

    public List<RespuestaMesaDto> getMesas() { return mesas; }
    public void setMesas(List<RespuestaMesaDto> mesas) { this.mesas = mesas; }

    public static class RespuestaMesaDto {
        private String id;
        private int numero;
        private List<RespuestaJuradoDto> jurados;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public int getNumero() { return numero; }
        public void setNumero(int numero) { this.numero = numero; }

        public List<RespuestaJuradoDto> getJurados() { return jurados; }
        public void setJurados(List<RespuestaJuradoDto> jurados) { this.jurados = jurados; }
    }
}
