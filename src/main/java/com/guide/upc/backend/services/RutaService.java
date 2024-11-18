package com.guide.upc.backend.services;

import com.guide.upc.backend.entities.Ruta;
import java.util.List;

public interface RutaService {
    Ruta guardarRuta(Ruta ruta);
    List<Ruta> obtenerRutasPorUsuario(Long usuarioId);
}
