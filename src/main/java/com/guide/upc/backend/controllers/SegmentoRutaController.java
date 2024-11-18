package com.guide.upc.backend.controllers;

import com.guide.upc.backend.entities.SegmentoRuta;
import com.guide.upc.backend.services.SegmentoRutaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/segmentos")
public class SegmentoRutaController {

    @Autowired
    private SegmentoRutaService segmentoRutaService;

    @GetMapping
    public ResponseEntity<List<SegmentoRuta>> obtenerTodosLosSegmentos() {
        List<SegmentoRuta> segmentos = segmentoRutaService.obtenerTodosLosSegmentos();
        return ResponseEntity.ok(segmentos);
    }
}
