package com.guide.upc.backend.services.impl;

import com.guide.upc.backend.entities.Lugar;
import com.guide.upc.backend.repositories.LugarRepository;
import com.guide.upc.backend.services.LugarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class LugarServiceImpl implements LugarService {

    @Autowired
    private LugarRepository lugarRepository;

    @Override
    public List<Lugar> findAll() {
        return lugarRepository.findAll();
    }

    @Override
    public Lugar findById(Long id) {
        return lugarRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lugar no encontrado"));
    }

    @Override
    public Lugar save(Lugar lugar) {
        return lugarRepository.save(lugar);
    }

    @Override
    public Lugar update(Long id, Lugar lugar) {
        Lugar existingLugar = lugarRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lugar no encontrado"));
        existingLugar.setNombre(lugar.getNombre());
        existingLugar.setDescripcion(lugar.getDescripcion());
        existingLugar.setFoto(lugar.getFoto());
        return lugarRepository.save(existingLugar);
    }

    @Override
    public void delete(Long id) {
        Lugar existingLugar = lugarRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lugar no encontrado"));
        lugarRepository.delete(existingLugar);
    }
}
