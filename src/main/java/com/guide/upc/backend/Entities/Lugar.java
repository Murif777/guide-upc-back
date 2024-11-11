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
@Table(name = "lugares")
public class Lugar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false)
    @Size(max = 100)
    private String nombre;

    @Column(name = "descripcion", nullable = false)
    @Size(max = 255)
    private String descripcion;

    @Column(name = "foto", nullable = true)
    private String foto;

    @Column(name = "latitud", nullable = false)
    private double latitud;

    @Column(name = "longitud", nullable = false)
    private double longitud;

    @Column(name = "icono", nullable = true)
    private String icono;
}
