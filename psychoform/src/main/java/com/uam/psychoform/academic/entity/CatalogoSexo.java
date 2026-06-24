package com.uam.psychoform.academic.entity;

import com.uam.psychoform.security.entity.EstadoGeneral;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "catalogo_sexo")
public class CatalogoSexo {
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
