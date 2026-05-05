package com.selloLegitimo.MockJurados.modelo;

import java.time.Instant;
import java.util.UUID;

import com.selloLegitimo.MockJurados.modelo.enums.EstadoAsistencia;

public class Asistencia {

    private String id;
    private String juradoId;
    private String mesaId;
    private EstadoAsistencia estado;
    private Instant fechaRegistro;
    private String observacion;

    public Asistencia() {
        this.id = UUID.randomUUID().toString();
        this.estado = EstadoAsistencia.PENDIENTE;
        this.fechaRegistro = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getJuradoId() { return juradoId; }
    public void setJuradoId(String juradoId) { this.juradoId = juradoId; }

    public String getMesaId() { return mesaId; }
    public void setMesaId(String mesaId) { this.mesaId = mesaId; }

    public EstadoAsistencia getEstado() { return estado; }
    public void setEstado(EstadoAsistencia estado) { this.estado = estado; }

    public Instant getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(Instant fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
}
