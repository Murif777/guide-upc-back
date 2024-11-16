package com.guide.upc.backend.services.impl;

import com.guide.upc.backend.entities.Ruta;
import com.guide.upc.backend.entities.User;
import com.guide.upc.backend.repositories.RutaRepository;
import com.guide.upc.backend.repositories.UserRepository;
import com.guide.upc.backend.services.RutaService;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RutaServiceImpl implements RutaService {

    @Autowired
    private RutaRepository rutaRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public Ruta crearRuta(Long usuarioId, String lugarPartida, String lugarLlegada, int distancia, String direccion) {
        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Ruta ruta = Ruta.builder()
                .usuario(usuario)
                .lugarPartida(lugarPartida)
                .lugarLlegada(lugarLlegada)
                .distancia(distancia)
                .direccion(direccion)
                .build();

        return rutaRepository.save(ruta);
    }

    @Override
    @Transactional
    public List<Ruta> obtenerRutasPorUsuario(Long usuarioId) {
        return rutaRepository.findByUsuarioId(usuarioId);
    }
}
