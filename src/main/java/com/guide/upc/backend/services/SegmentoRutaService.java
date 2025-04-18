package com.guide.upc.backend.services;

import com.guide.upc.backend.entities.SegmentoRuta;

import java.util.List;
import java.util.Map;

public interface SegmentoRutaService {
    List<SegmentoRuta> obtenerTodosLosSegmentos();
    Map<String, Object> obtenerMejorRuta(String origen, String destino);
}
