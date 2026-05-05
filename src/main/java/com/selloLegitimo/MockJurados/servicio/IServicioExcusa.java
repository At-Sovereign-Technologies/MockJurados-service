package com.selloLegitimo.MockJurados.servicio;

import com.selloLegitimo.MockJurados.dto.RespuestaExcusaDto;
import com.selloLegitimo.MockJurados.dto.SolicitudExcusaDto;
import com.selloLegitimo.MockJurados.dto.SolicitudResolverExcusaDto;
import java.util.List;

public interface IServicioExcusa {

    RespuestaExcusaDto presentarExcusa(SolicitudExcusaDto solicitud);

    RespuestaExcusaDto resolverExcusa(String excusaId, SolicitudResolverExcusaDto resolucion);

    RespuestaExcusaDto buscarPorId(String id);

    List<RespuestaExcusaDto> listarTodos();
}
