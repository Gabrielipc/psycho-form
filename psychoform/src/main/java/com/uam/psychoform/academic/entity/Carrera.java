package com.uam.psychoform.academic.entity;

import com.uam.psychoform.security.entity.EstadoGeneral;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "carrera")
public class Carrera {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "carrera_id")
    private Short id;
    @NotBlank
    @Size(max = 30)
    @Column(name = "codigo_carrera", nullable = false, length = 30)
    private String codigoCarrera;
    @NotBlank
    @Size(max = 150)
    @Column(name = "nombre_carrera", nullable = false, length = 150)
    private String nombreCarrera;
    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "estado_general")
    private EstadoGeneral estado;
}
