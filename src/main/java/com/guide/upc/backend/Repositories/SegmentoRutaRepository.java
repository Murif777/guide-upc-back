package com.guide.upc.backend.repositories;

import com.guide.upc.backend.entities.SegmentoRuta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SegmentoRutaRepository extends JpaRepository<SegmentoRuta, Long> {
    @SuppressWarnings("null")
    List<SegmentoRuta> findAll();
}
