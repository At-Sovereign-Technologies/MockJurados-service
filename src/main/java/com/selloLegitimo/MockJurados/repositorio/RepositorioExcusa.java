package com.selloLegitimo.MockJurados.repositorio;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.selloLegitimo.MockJurados.modelo.Excusa;
import com.selloLegitimo.MockJurados.modelo.enums.EstadoExcusa;

@Repository
public class RepositorioExcusa {

    private final Map<String, Excusa> store = new ConcurrentHashMap<>();

    public Excusa guardar(Excusa excusa) {
        store.put(excusa.getId(), excusa);
        return excusa;
    }

    public Excusa buscarPorId(String id) {
        return store.get(id);
    }

    public List<Excusa> buscarPorJurado(String juradoId) {
        List<Excusa> resultado = new ArrayList<>();
        for (Excusa e : store.values()) {
            if (juradoId.equals(e.getJuradoId())) {
                resultado.add(e);
            }
        }
        return resultado;
    }

    public List<Excusa> buscarPorEstado(EstadoExcusa estado) {
        List<Excusa> resultado = new ArrayList<>();
        for (Excusa e : store.values()) {
            if (estado == e.getEstado()) {
                resultado.add(e);
            }
        }
        return resultado;
    }

    public List<Excusa> buscarTodos() {
        return new ArrayList<>(store.values());
    }

    public long contar() {
        return store.size();
    }

    public void limpiar() {
        store.clear();
    }
}
