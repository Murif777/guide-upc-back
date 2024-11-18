package com.guide.upc.backend.controllers;

import com.guide.upc.backend.entities.Ruta;
import com.guide.upc.backend.services.RutaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rutas")
public class RutaController {

    @Autowired
    private RutaService rutaService;

    @PostMapping("/save")
    public ResponseEntity<Ruta> guardarRuta(@RequestBody Ruta ruta) {
        Ruta nuevaRuta = rutaService.guardarRuta(ruta);
        return ResponseEntity.ok(nuevaRuta);
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Ruta>> obtenerRutasPorUsuario(@PathVariable Long usuarioId) {
        List<Ruta> rutas = rutaService.obtenerRutasPorUsuario(usuarioId);
        return ResponseEntity.ok(rutas);
    }
}
