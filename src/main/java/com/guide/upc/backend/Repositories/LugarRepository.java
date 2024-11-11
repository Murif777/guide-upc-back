package com.guide.upc.backend.repositories;

import com.guide.upc.backend.entities.Lugar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LugarRepository extends JpaRepository<Lugar, Long> {
    // Puedes añadir métodos personalizados aquí si es necesario
}
