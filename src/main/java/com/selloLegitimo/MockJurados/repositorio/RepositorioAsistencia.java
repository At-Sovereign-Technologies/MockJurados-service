package com.selloLegitimo.MockJurados.repositorio;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.selloLegitimo.MockJurados.modelo.Asistencia;

@Repository
public class RepositorioAsistencia {

    private final Map<String, Asistencia> store = new ConcurrentHashMap<>();

    public Asistencia guardar(Asistencia asistencia) {
        store.put(asistencia.getId(), asistencia);
        return asistencia;
    }

    public Asistencia buscarPorId(String id) {
        return store.get(id);
    }

    public List<Asistencia> buscarPorMesa(String mesaId) {
        List<Asistencia> resultado = new ArrayList<>();
        for (Asistencia a : store.values()) {
            if (mesaId.equals(a.getMesaId())) {
                resultado.add(a);
            }
        }
        return resultado;
    }

    public List<Asistencia> buscarPorJurado(String juradoId) {
        List<Asistencia> resultado = new ArrayList<>();
        for (Asistencia a : store.values()) {
            if (juradoId.equals(a.getJuradoId())) {
                resultado.add(a);
            }
        }
        return resultado;
    }

    public List<Asistencia> buscarTodos() {
        return new ArrayList<>(store.values());
    }

    public long contar() {
        return store.size();
    }

    public void limpiar() {
        store.clear();
    }
}
