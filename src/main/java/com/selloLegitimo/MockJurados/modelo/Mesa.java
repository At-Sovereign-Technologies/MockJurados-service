package com.selloLegitimo.MockJurados.modelo;

import java.util.ArrayList;
import java.util.List;

public class Mesa {

    private String id;
    private int numero;
    private String puestoId;
    private String departamento;
    private String municipio;
    private List<String> juradoIds;

    public Mesa() {
        this.juradoIds = new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }

    public String getPuestoId() { return puestoId; }
    public void setPuestoId(String puestoId) { this.puestoId = puestoId; }

    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }

    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }

    public List<String> getJuradoIds() { return juradoIds; }
    public void setJuradoIds(List<String> juradoIds) { this.juradoIds = juradoIds; }
}
