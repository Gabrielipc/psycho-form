package com.uam.psychoform.academic.entity;

import com.uam.psychoform.security.entity.EstadoGeneral;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "grupo_academico", uniqueConstraints = @UniqueConstraint(columnNames = { "carrera_id", "codigo_grupo" }))
public class GrupoAcademico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "grupo_academico_id")
    private Short id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrera_id")
    private Carrera carrera;
    @NotBlank
    @Size(max = 50)
    @Column(name = "codigo_grupo", nullable = false, length = 50)
    private String codigoGrupo;
    @Size(max = 100)
    @Column(name = "nombre_grupo", length = 100)
    private String nombreGrupo;
    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "estado_general")
    private EstadoGeneral estado;
}
