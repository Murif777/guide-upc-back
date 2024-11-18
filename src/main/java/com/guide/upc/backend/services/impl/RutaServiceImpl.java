package com.guide.upc.backend.services.impl;

import com.guide.upc.backend.entities.Ruta;
import com.guide.upc.backend.entities.User;
import com.guide.upc.backend.repositories.RutaRepository;
import com.guide.upc.backend.repositories.UserRepository;
import com.guide.upc.backend.services.RutaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class RutaServiceImpl implements RutaService {

    private final RutaRepository rutaRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Ruta guardarRuta(Ruta ruta) {
        User usuario = userRepository.findById(ruta.getUsuario().getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        ruta.setUsuario(usuario);
        return rutaRepository.save(ruta);
    }

    @Override
    public List<Ruta> obtenerRutasPorUsuario(Long usuarioId) {
        return rutaRepository.findByUsuarioId(usuarioId);
    }
}
