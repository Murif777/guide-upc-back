package com.guide.upc.backend.repositories;

import com.guide.upc.backend.entities.Lugar;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface LugarRepository extends JpaRepository<Lugar, Long> {
    @SuppressWarnings("null")
    Optional<Lugar> findById(Long id);
    
    List<Lugar> findByNombreIn(Set<String> nombres);

}
