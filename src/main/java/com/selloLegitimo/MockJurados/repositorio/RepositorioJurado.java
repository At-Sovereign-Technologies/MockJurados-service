package com.selloLegitimo.MockJurados.repositorio;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.selloLegitimo.MockJurados.modelo.Jurado;

@Repository
public class RepositorioJurado {

    private final Map<String, Jurado> store = new ConcurrentHashMap<>();

    public Jurado guardar(Jurado jurado) {
        store.put(jurado.getId(), jurado);
        return jurado;
    }

    public Jurado buscarPorId(String id) {
        return store.get(id);
    }

    public List<Jurado> buscarPorMesa(String mesaId) {
        List<Jurado> resultado = new ArrayList<>();
        for (Jurado j : store.values()) {
            if (mesaId.equals(j.getMesaId())) {
                resultado.add(j);
            }
        }
        return resultado;
    }

    public List<Jurado> buscarPorCedula(String cedula) {
        List<Jurado> resultado = new ArrayList<>();
        for (Jurado j : store.values()) {
            if (cedula.equals(j.getCedula())) {
                resultado.add(j);
            }
        }
        return resultado;
    }

    public List<Jurado> buscarTodos() {
        return new ArrayList<>(store.values());
    }

    public boolean existeCedulaEnSorteo(String cedula) {
        return store.values().stream().anyMatch(j -> cedula.equals(j.getCedula()));
    }

    public long contar() {
        return store.size();
    }

    public void eliminar(String id) {
        store.remove(id);
    }

    public void limpiar() {
        store.clear();
    }
}
