package com.selloLegitimo.MockJurados.servicio;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.selloLegitimo.MockJurados.dto.EventoOutboxDto;
import com.selloLegitimo.MockJurados.modelo.EventoOutbox;
import com.selloLegitimo.MockJurados.modelo.enums.TipoEvento;
import com.selloLegitimo.MockJurados.repositorio.RepositorioOutbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ServicioOutbox {

    private static final Logger log = LoggerFactory.getLogger(ServicioOutbox.class);

    private final RepositorioOutbox repositorioOutbox;
    private final ObjectMapper objectMapper;

    public ServicioOutbox(RepositorioOutbox repositorioOutbox, ObjectMapper objectMapper) {
        this.repositorioOutbox = repositorioOutbox;
        this.objectMapper = objectMapper;
    }

    public void emitir(String tipo, Map<String, Object> datos) {
        try {
            EventoOutbox evento = new EventoOutbox();
            evento.setTipo(TipoEvento.valueOf(tipo));
            evento.setPayload(objectMapper.writeValueAsString(datos));
            repositorioOutbox.guardar(evento);
            log.debug("Evento emitido: tipo={}, id={}", tipo, evento.getId());
        } catch (Exception e) {
            log.error("Error al emitir evento: tipo={}", tipo, e);
        }
    }

    public List<EventoOutboxDto> listarNoEmitidos() {
        return repositorioOutbox.buscarNoEmitidos()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<EventoOutboxDto> listarTodos() {
        return repositorioOutbox.buscarTodos()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public void marcarEmitido(String id) {
        repositorioOutbox.marcarEmitido(id);
    }

    public long contar() {
        return repositorioOutbox.contar();
    }

    public void limpiar() {
        repositorioOutbox.limpiar();
    }

    private EventoOutboxDto toDto(EventoOutbox e) {
        EventoOutboxDto dto = new EventoOutboxDto();
        dto.setId(e.getId());
        dto.setTipo(e.getTipo().name());
        dto.setPayload(e.getPayload());
        dto.setEmitido(e.isEmitido());
        dto.setFechaEmision(e.getFechaEmision());
        return dto;
    }
}
