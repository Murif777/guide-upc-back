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

    @PostMapping
    public ResponseEntity<Ruta> crearRuta(
            @RequestParam Long usuarioId,
            @RequestParam String puntoPartida,
            @RequestParam String puntoLlegada,
            @RequestParam int distanciaEnPasos,
            @RequestParam String direccion) {

        Ruta nuevaRuta = rutaService.crearRuta(usuarioId, puntoPartida, puntoLlegada, distanciaEnPasos, direccion);
        return ResponseEntity.ok(nuevaRuta);
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Ruta>> obtenerRutasPorUsuario(@PathVariable Long usuarioId) {
        List<Ruta> rutas = rutaService.obtenerRutasPorUsuario(usuarioId);
        return ResponseEntity.ok(rutas);
    }
}
