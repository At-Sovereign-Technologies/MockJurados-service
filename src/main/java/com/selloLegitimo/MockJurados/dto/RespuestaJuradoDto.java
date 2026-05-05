package com.selloLegitimo.MockJurados.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;

public class RespuestaJuradoDto {

    private String id;
    private String cedula;
    private String nombre;
    private String apellido;
    private String mesaId;
    private String puestoId;
    private String rol;
    private String estado;
    private String reemplazaA;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant fechaCreacion;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getMesaId() { return mesaId; }
    public void setMesaId(String mesaId) { this.mesaId = mesaId; }

    public String getPuestoId() { return puestoId; }
    public void setPuestoId(String puestoId) { this.puestoId = puestoId; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getReemplazaA() { return reemplazaA; }
    public void setReemplazaA(String reemplazaA) { this.reemplazaA = reemplazaA; }

    public Instant getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Instant fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
