package com.selloLegitimo.MockJurados.servicio;

import com.selloLegitimo.MockJurados.dto.RespuestaAsistenciaDto;
import com.selloLegitimo.MockJurados.dto.SolicitudAsistenciaDto;
import java.util.List;

public interface IServicioAsistencia {

    RespuestaAsistenciaDto registrarAsistencia(SolicitudAsistenciaDto solicitud);

    List<RespuestaAsistenciaDto> consultarPorMesa(String mesaId);

    List<RespuestaAsistenciaDto> listarTodos();
}
