package com.selloLegitimo.MockJurados.excepcion;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ManejadorGlobalExcepciones {

    @ExceptionHandler(ExcepcionReglaNegocio.class)
    public ResponseEntity<Map<String, Object>> manejarReglaNegocio(ExcepcionReglaNegocio ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("fecha", LocalDateTime.now().toString());
        body.put("codigo", HttpStatus.BAD_REQUEST.value());
        body.put("mensaje", ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ExcepcionRecursoNoEncontrado.class)
    public ResponseEntity<Map<String, Object>> manejarNoEncontrado(ExcepcionRecursoNoEncontrado ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("fecha", LocalDateTime.now().toString());
        body.put("codigo", HttpStatus.NOT_FOUND.value());
        body.put("mensaje", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarValidacion(MethodArgumentNotValidException ex) {
        List<String> errores = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.toList());

        Map<String, Object> body = new HashMap<>();
        body.put("fecha", LocalDateTime.now().toString());
        body.put("codigo", HttpStatus.BAD_REQUEST.value());
        body.put("mensaje", "Error de validacion");
        body.put("errores", errores);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> manejarArgumentoInvalido(IllegalArgumentException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("fecha", LocalDateTime.now().toString());
        body.put("codigo", HttpStatus.BAD_REQUEST.value());
        body.put("mensaje", ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> manejarGeneral(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("fecha", LocalDateTime.now().toString());
        body.put("codigo", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("mensaje", "Error interno del servidor: " + ex.getMessage());
        return ResponseEntity.internalServerError().body(body);
    }
}
