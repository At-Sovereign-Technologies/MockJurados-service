package com.selloLegitimo.MockJurados.controlador;

import com.selloLegitimo.MockJurados.dto.EventoOutboxDto;
import com.selloLegitimo.MockJurados.dto.RespuestaAsistenciaDto;
import com.selloLegitimo.MockJurados.dto.RespuestaExcusaDto;
import com.selloLegitimo.MockJurados.dto.RespuestaJuradoDto;
import com.selloLegitimo.MockJurados.servicio.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mock")
public class ControladorMockDebug {

    private static final Logger log = LoggerFactory.getLogger(ControladorMockDebug.class);

    private final IServicioJurados servicioJurados;
    private final IServicioExcusa servicioExcusa;
    private final IServicioAsistencia servicioAsistencia;
    private final ServicioOutbox servicioOutbox;

    public ControladorMockDebug(IServicioJurados servicioJurados,
                                IServicioExcusa servicioExcusa,
                                IServicioAsistencia servicioAsistencia,
                                ServicioOutbox servicioOutbox) {
        this.servicioJurados = servicioJurados;
        this.servicioExcusa = servicioExcusa;
        this.servicioAsistencia = servicioAsistencia;
        this.servicioOutbox = servicioOutbox;
    }

    @GetMapping("/jurados")
    public ResponseEntity<List<RespuestaJuradoDto>> listarJurados() {
        return ResponseEntity.ok(servicioJurados.listarTodos());
    }

    @GetMapping("/excusas")
    public ResponseEntity<List<RespuestaExcusaDto>> listarExcusas() {
        return ResponseEntity.ok(servicioExcusa.listarTodos());
    }

    @GetMapping("/asistencias")
    public ResponseEntity<List<RespuestaAsistenciaDto>> listarAsistencias() {
        return ResponseEntity.ok(servicioAsistencia.listarTodos());
    }

    @GetMapping("/outbox")
    public ResponseEntity<List<EventoOutboxDto>> listarOutbox() {
        return ResponseEntity.ok(servicioOutbox.listarTodos());
    }

    @GetMapping("/state")
    public ResponseEntity<Map<String, Object>> estadoCompleto() {
        Map<String, Object> state = new HashMap<>();
        state.put("totalJurados", servicioJurados.listarTodos().size());
        state.put("totalExcusas", servicioExcusa.listarTodos().size());
        state.put("totalAsistencias", servicioAsistencia.listarTodos().size());
        state.put("totalEventosOutbox", servicioOutbox.contar());
        state.put("timestamp", java.time.Instant.now().toString());
        return ResponseEntity.ok(state);
    }

    @DeleteMapping("/reset")
    public ResponseEntity<Map<String, String>> resetear() {
        servicioJurados.limpiar();
        servicioOutbox.limpiar();
        log.warn("Estado del mock reiniciado");
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Estado del mock reiniciado exitosamente");
        return ResponseEntity.ok(response);
    }
}
