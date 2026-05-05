package com.selloLegitimo.MockJurados.repositorio;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.selloLegitimo.MockJurados.modelo.Mesa;

@Repository
public class RepositorioMesa {

    private final Map<String, Mesa> store = new ConcurrentHashMap<>();

    public Mesa guardar(Mesa mesa) {
        store.put(mesa.getId(), mesa);
        return mesa;
    }

    public Mesa buscarPorId(String id) {
        return store.get(id);
    }

    public List<Mesa> buscarTodos() {
        return new ArrayList<>(store.values());
    }

    public long contar() {
        return store.size();
    }

    public void limpiar() {
        store.clear();
    }
}
