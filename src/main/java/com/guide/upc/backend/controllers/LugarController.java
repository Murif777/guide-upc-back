package com.guide.upc.backend.controllers;

import com.guide.upc.backend.entities.Lugar;
import com.guide.upc.backend.services.LugarService;
import com.guide.upc.backend.services.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/lugares")
public class LugarController {

    @Autowired
    private LugarService lugarService;
    @Autowired 
    private StorageService storageService;

    @GetMapping
    public ResponseEntity<List<Lugar>> getAllLugares() {
        List<Lugar> lugares = lugarService.findAll();
        return ResponseEntity.ok(lugares);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Lugar> getLugarById(@PathVariable Long id) {
        Lugar lugar = lugarService.findById(id);
        return ResponseEntity.ok(lugar);
    }

    @PostMapping
    public ResponseEntity<Lugar> createLugar(@RequestBody Lugar lugar) {
        Lugar newLugar = lugarService.save(lugar);
        return ResponseEntity.ok(newLugar);
    }

    @PostMapping("/create-with-image")
    public ResponseEntity<Lugar> createLugarWithImage(
        @RequestParam("nombre") String nombre,
        @RequestParam("descripcion") String descripcion,
        @RequestParam("latitud") double latitud,
        @RequestParam("longitud") double longitud,
        @RequestParam("icono") String icono,
        @RequestParam("file") MultipartFile file) {

        try {
            // Guardar la imagen utilizando StorageService
            String fotoUrl = storageService.store(file);

            // Crear el objeto Lugar y establecer la URL de la imagen
            Lugar lugar = Lugar.builder()
                    .nombre(nombre)
                    .descripcion(descripcion)
                    .latitud(latitud)
                    .longitud(longitud)
                    .icono(icono)
                    .foto(fotoUrl)
                    .build();

            // Guardar el objeto Lugar en la base de datos
            Lugar newLugar = lugarService.save(lugar);
            return ResponseEntity.ok(newLugar);

        } catch (IOException e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Lugar> updateLugar(@PathVariable Long id, @RequestBody Lugar lugar) {
        Lugar updatedLugar = lugarService.update(id, lugar);
        return ResponseEntity.ok(updatedLugar);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLugar(@PathVariable Long id) {
        lugarService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
