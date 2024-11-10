package com.guide.upc.backend.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@Table(name = "usuarios")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false)
    @Size(max = 100)
    private String nombre;

    @Column(name = "apellido", nullable = false)
    @Size(max = 100)
    private String apellido;

    @Column(nullable = false)
    @Size(max = 100)
    private String login;

    @Column(nullable = false)
    @Size(max = 100)
    private String contraseña;

    @Column(name = "foto", nullable = true)
    private String foto;
}
