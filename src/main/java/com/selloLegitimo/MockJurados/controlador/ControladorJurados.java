package com.selloLegitimo.MockJurados.controlador;

import com.selloLegitimo.MockJurados.dto.*;
import com.selloLegitimo.MockJurados.servicio.IServicioAsistencia;
import com.selloLegitimo.MockJurados.servicio.IServicioExcusa;
import com.selloLegitimo.MockJurados.servicio.IServicioJurados;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jurados")
public class ControladorJurados {

    private static final Logger log = LoggerFactory.getLogger(ControladorJurados.class);

    private final IServicioJurados servicioJurados;
    private final IServicioExcusa servicioExcusa;
    private final IServicioAsistencia servicioAsistencia;

    public ControladorJurados(IServicioJurados servicioJurados,
                              IServicioExcusa servicioExcusa,
                              IServicioAsistencia servicioAsistencia) {
        this.servicioJurados = servicioJurados;
        this.servicioExcusa = servicioExcusa;
        this.servicioAsistencia = servicioAsistencia;
    }

    @PostMapping("/sorteo")
    public ResponseEntity<RespuestaSorteoDto> realizarSorteo(
            @Valid @RequestBody SolicitudSorteoDto solicitud) {
        log.info("POST /api/jurados/sorteo - eleccionId={}, mesas={}",
                solicitud.getEleccionId(), solicitud.getNumeroMesas());
        RespuestaSorteoDto respuesta = servicioJurados.realizarSorteo(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @PostMapping("/excusa")
    public ResponseEntity<RespuestaExcusaDto> presentarExcusa(
            @Valid @RequestBody SolicitudExcusaDto solicitud) {
        log.info("POST /api/jurados/excusa - juradoId={}", solicitud.getJuradoId());
        RespuestaExcusaDto respuesta = servicioExcusa.presentarExcusa(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @PostMapping("/excusa/{id}/resolver")
    public ResponseEntity<RespuestaExcusaDto> resolverExcusa(
            @PathVariable String id,
            @Valid @RequestBody SolicitudResolverExcusaDto resolucion) {
        log.info("POST /api/jurados/excusa/{}/resolver - estado={}", id, resolucion.getEstado());
        RespuestaExcusaDto respuesta = servicioExcusa.resolverExcusa(id, resolucion);
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping("/asistencia")
    public ResponseEntity<RespuestaAsistenciaDto> registrarAsistencia(
            @Valid @RequestBody SolicitudAsistenciaDto solicitud) {
        log.info("POST /api/jurados/asistencia - juradoId={}, estado={}",
                solicitud.getJuradoId(), solicitud.getEstado());
        RespuestaAsistenciaDto respuesta = servicioAsistencia.registrarAsistencia(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @GetMapping("/mesa/{mesaId}")
    public ResponseEntity<java.util.List<RespuestaJuradoDto>> consultarJuradosPorMesa(
            @PathVariable String mesaId) {
        log.info("GET /api/jurados/mesa/{}", mesaId);
        return ResponseEntity.ok(servicioJurados.buscarPorMesa(mesaId));
    }

    @GetMapping("/cedula/{cedula}")
    public ResponseEntity<RespuestaJuradoDto> consultarPorCedula(@PathVariable String cedula) {
        return ResponseEntity.ok(servicioJurados.buscarPorCedula(cedula));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RespuestaJuradoDto> consultarPorId(@PathVariable String id) {
        return ResponseEntity.ok(servicioJurados.buscarPorId(id));
    }
}
