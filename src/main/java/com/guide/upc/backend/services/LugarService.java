package com.guide.upc.backend.services;

import com.guide.upc.backend.entities.Lugar;
import java.util.List;

public interface LugarService {
    List<Lugar> findAll();
    Lugar findById(Long id);
    Lugar save(Lugar lugar);
    Lugar update(Long id, Lugar lugar);
    void delete(Long id);
}
