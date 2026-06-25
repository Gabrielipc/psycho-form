package com.uam.psychoform.academic.model;

import com.uam.psychoform.security.model.EstadoGeneral;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "catalogo_sexo")
@Getter
@Setter
public class CatalogoSexo implements CatalogoEntidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sexo_id")
    private Short id;
    @NotBlank
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String codigo;
    @NotBlank
    @Size(max = 80)
    @Column(nullable = false, length = 80)
    private String nombre;
    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "estado_general")
    private EstadoGeneral estado;
}
