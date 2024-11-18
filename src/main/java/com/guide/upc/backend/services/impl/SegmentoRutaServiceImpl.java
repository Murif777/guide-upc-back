package com.guide.upc.backend.services.impl;

import com.guide.upc.backend.entities.SegmentoRuta;
import com.guide.upc.backend.repositories.SegmentoRutaRepository;
import com.guide.upc.backend.services.SegmentoRutaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class SegmentoRutaServiceImpl implements SegmentoRutaService {

    private final SegmentoRutaRepository segmentoRutaRepository;

    @Override
    public List<SegmentoRuta> obtenerTodosLosSegmentos() {
        return segmentoRutaRepository.findAll();
    }
}
