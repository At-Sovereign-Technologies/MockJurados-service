package com.selloLegitimo.MockJurados.excepcion;

public class ExcepcionRecursoNoEncontrado extends RuntimeException {

    public ExcepcionRecursoNoEncontrado(String recurso, String id) {
        super(recurso + " no encontrado con id: " + id);
    }
}
