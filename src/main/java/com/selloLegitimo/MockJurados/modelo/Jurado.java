package com.selloLegitimo.MockJurados.modelo;

import java.time.Instant;
import java.util.UUID;

import com.selloLegitimo.MockJurados.modelo.enums.EstadoJurado;
import com.selloLegitimo.MockJurados.modelo.enums.RolJurado;

public class Jurado {

    private String id;
    private String cedula;
    private String nombre;
    private String apellido;
    private String mesaId;
    private String puestoId;
    private RolJurado rol;
    private EstadoJurado estado;
    private String reemplazaA;
    private Instant fechaCreacion;

    public Jurado() {
        this.id = UUID.randomUUID().toString();
        this.estado = EstadoJurado.ASIGNADO;
        this.fechaCreacion = Instant.now();
    }

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

    public RolJurado getRol() { return rol; }
    public void setRol(RolJurado rol) { this.rol = rol; }

    public EstadoJurado getEstado() { return estado; }
    public void setEstado(EstadoJurado estado) { this.estado = estado; }

    public String getReemplazaA() { return reemplazaA; }
    public void setReemplazaA(String reemplazaA) { this.reemplazaA = reemplazaA; }

    public Instant getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Instant fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
