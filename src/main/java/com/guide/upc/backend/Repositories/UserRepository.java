package com.guide.upc.backend.repositories;

import com.guide.upc.backend.entities.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @EntityGraph(attributePaths = {"rutas"})
    Optional<User> findByLogin(String login);
}
