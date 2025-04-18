package com.guide.upc.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.guide.upc.backend.services.AINavegacionService;

@RestController
@RequestMapping("/api/ia-navegacion")
public class AINavegacionController {

    @Autowired
    private AINavegacionService aiNavegacionService;

    @PostMapping("/consulta")
    public ResponseEntity<String> procesarConsulta(@RequestBody ConsultaDTO consulta) {
        String respuesta = aiNavegacionService.procesarConsulta(consulta.getTexto());
        return ResponseEntity.ok(respuesta);
    }
}

// Mantener el ConsultaDTO aquí
class ConsultaDTO {
    private String texto;
    
    public String getTexto() {
        return texto;
    }
    
    public void setTexto(String texto) {
        this.texto = texto;
    }
}