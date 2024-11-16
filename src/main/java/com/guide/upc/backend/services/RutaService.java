package com.guide.upc.backend.services;

import com.guide.upc.backend.entities.Ruta;

import java.util.List;

public interface RutaService {
    Ruta crearRuta(Long usuarioId, String lugarPartida, String lugarLlegada, int distancia, String direccion);
    List<Ruta> obtenerRutasPorUsuario(Long usuarioId);
}
