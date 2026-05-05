package com.selloLegitimo.MockJurados.repositorio;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.selloLegitimo.MockJurados.modelo.EventoOutbox;

@Repository
public class RepositorioOutbox {

    private final Map<String, EventoOutbox> store = new ConcurrentHashMap<>();

    public EventoOutbox guardar(EventoOutbox evento) {
        store.put(evento.getId(), evento);
        return evento;
    }

    public List<EventoOutbox> buscarNoEmitidos() {
        List<EventoOutbox> resultado = new ArrayList<>();
        for (EventoOutbox e : store.values()) {
            if (!e.isEmitido()) {
                resultado.add(e);
            }
        }
        return resultado;
    }

    public void marcarEmitido(String id) {
        EventoOutbox e = store.get(id);
        if (e != null) {
            e.setEmitido(true);
        }
    }

    public List<EventoOutbox> buscarTodos() {
        return new ArrayList<>(store.values());
    }

    public long contar() {
        return store.size();
    }

    public void limpiar() {
        store.clear();
    }
}
