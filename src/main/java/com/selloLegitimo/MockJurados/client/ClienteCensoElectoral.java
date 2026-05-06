package com.selloLegitimo.MockJurados.client;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Component
public class ClienteCensoElectoral {

    private static final Logger log = LoggerFactory.getLogger(ClienteCensoElectoral.class);

    private final RestClient restClient;
    private final String baseUrl;

    public ClienteCensoElectoral(RestClient.Builder restClientBuilder,
            @org.springframework.beans.factory.annotation.Value("${censo.service.url:}") String baseUrl) {
        this.baseUrl = baseUrl;
        this.restClient = restClientBuilder.build();
    }

    public boolean isDisponible() {
        return baseUrl != null && !baseUrl.isBlank();
    }

    public List<CiudadanoCensoDto> obtenerCiudadanosHabilitados(Long eleccionId, String departamento, String municipio) {
        if (!isDisponible()) {
            log.warn("Servicio de censo electoral no configurado (censo.service.url vacio)");
            return List.of();
        }

        String url = baseUrl + "/api/censo/elecciones/" + eleccionId + "/registros"
                + "?tamano=10000&estado=HABILITADO";
        if (departamento != null && !departamento.isBlank()) {
            url += "&departamento=" + departamento;
            if (municipio != null && !municipio.isBlank()) {
                url += "&municipio=" + municipio;
            }
        }

        try {
            ResponseEntity<PaginaCensoDto> respuesta = restClient.get()
                    .uri(url)
                    .retrieve()
                    .toEntity(PaginaCensoDto.class);

            if (respuesta.getBody() == null || respuesta.getBody().contenido == null
                    || respuesta.getBody().contenido.isEmpty()) {
                log.info("Censo servicio no devolvio registros para eleccion={}, depto={}, mun={}",
                        eleccionId, departamento, municipio);
                return List.of();
            }

            List<CiudadanoCensoDto> ciudadanos = respuesta.getBody().contenido.stream()
                    .filter(r -> "HABILITADO".equalsIgnoreCase(r.estado))
                    .filter(r -> departamento == null || departamento.isBlank()
                            || departamento.equalsIgnoreCase(r.departamento))
                    .filter(r -> municipio == null || municipio.isBlank()
                            || municipio.equalsIgnoreCase(r.municipio))
                    .map(r -> new CiudadanoCensoDto(
                            r.tipoDocumento, r.numeroDocumento,
                            r.nombres, r.apellidos,
                            r.departamento, r.municipio))
                    .toList();

            log.info("Obtenidos {} ciudadanos HABILITADOS del censo para eleccion={}, depto={}, mun={}",
                    ciudadanos.size(), eleccionId, departamento, municipio);
            return ciudadanos;
        } catch (Exception ex) {
            log.warn("No fue posible consultar el servicio de censo electoral: {}", ex.getMessage());
            return List.of();
        }
    }

    public List<String> obtenerCandidatosEnEleccion(Long eleccionId) {
        if (!isDisponible()) {
            return List.of();
        }

        try {
            String url = baseUrl + "/api/candidaturas/elecciones/" + eleccionId;
            ResponseEntity<CandidaturaDto[]> respuesta = restClient.get()
                    .uri(url)
                    .retrieve()
                    .toEntity(CandidaturaDto[].class);

            if (respuesta.getBody() == null) {
                return List.of();
            }

            return java.util.Arrays.stream(respuesta.getBody())
                    .map(c -> c.documento)
                    .toList();
        } catch (Exception ex) {
            log.warn("No fue posible consultar candidaturas para eleccion={}: {}", eleccionId, ex.getMessage());
            return List.of();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PaginaCensoDto {
        public List<RegistroCensoDto> contenido;
        public long totalElementos;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RegistroCensoDto {
        public Long id;
        public Long eleccionId;
        public String tipoDocumento;
        public String numeroDocumento;
        public String nombres;
        public String apellidos;
        public String fechaNacimiento;
        public String departamento;
        public String municipio;
        public String estado;
        public String causalEstado;
        public String observacion;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CandidaturaDto {
        public Long id;
        public Long eleccionId;
        public String documento;
        public String nombreCandidato;
        public String partido;
        public String estado;
    }

    public static class CiudadanoCensoDto {
        public final String tipoDocumento;
        public final String numeroDocumento;
        public final String nombres;
        public final String apellidos;
        public final String departamento;
        public final String municipio;

        public CiudadanoCensoDto(String tipoDocumento, String numeroDocumento,
                String nombres, String apellidos, String departamento, String municipio) {
            this.tipoDocumento = tipoDocumento;
            this.numeroDocumento = numeroDocumento;
            this.nombres = nombres;
            this.apellidos = apellidos;
            this.departamento = departamento;
            this.municipio = municipio;
        }
    }
}