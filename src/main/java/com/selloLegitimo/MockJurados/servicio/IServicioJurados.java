package com.selloLegitimo.MockJurados.servicio;

import com.selloLegitimo.MockJurados.dto.RespuestaJuradoDto;
import com.selloLegitimo.MockJurados.dto.RespuestaSorteoDto;
import com.selloLegitimo.MockJurados.dto.SolicitudSorteoDto;
import java.util.List;

public interface IServicioJurados {

    RespuestaSorteoDto realizarSorteo(SolicitudSorteoDto solicitud);

    RespuestaJuradoDto buscarPorId(String id);

    RespuestaJuradoDto buscarPorCedula(String cedula);

    List<RespuestaJuradoDto> buscarPorMesa(String mesaId);

    List<RespuestaJuradoDto> listarTodos();

    void limpiar();
}
