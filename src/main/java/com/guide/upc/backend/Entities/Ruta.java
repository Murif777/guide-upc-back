package com.guide.upc.backend.entities;

//import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@Table(name = "rutas")
public class Ruta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "usuario_id")
   // @JsonManagedReference  Evitar recursión infinita
    private User usuario;

    @NotNull
    private String lugarPartida;

    @NotNull
    private String lugarLlegada;

    @Override
    public String toString() {
        return "Ruta{id=" + id + ", lugarPartida='" + lugarPartida + '\'' + ", lugarLlegada='" + lugarLlegada + '\'' + '}';
    }
}
