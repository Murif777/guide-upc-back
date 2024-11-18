package com.guide.upc.backend.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@Table(name = "usuarios")
public class User {

    @Id
    @Column(name = "id", nullable = false)
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

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL) 
    @JsonBackReference // Evitar recursión infinita
    private List<Ruta> rutas;

    @Override
    public String toString() {
        return "User{id=" + id + ", nombre='" + nombre + '\'' + ", apellido='" + apellido + '\'' + '}';
    }
}
