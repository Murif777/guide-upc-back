package com.guide.upc.backend.repositories;

import com.guide.upc.backend.entities.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RutaRepository extends JpaRepository<Ruta, Long> {
    
    List<Ruta> findByUsuarioId(Long usuarioId);
}
