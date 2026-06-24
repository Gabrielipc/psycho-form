package com.uam.psychoform.academic.entity;

import com.uam.psychoform.security.entity.EstadoGeneral;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "participante")
public class Participante {
    @Id
    @Column(name = "participante_id")
    private UUID id;
    @NotBlank
    @Size(max = 80)
    @Column(name = "codigo_participante", nullable = false, length = 80)
    private String codigoParticipante;
    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nombres;
    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String apellidos;
    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sexo_id")
    private CatalogoSexo sexo;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrera_id")
    private Carrera carrera;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cohorte_id")
    private Cohorte cohorte;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_academico_id")
    private GrupoAcademico grupoAcademico;
    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "estado_general")
    private EstadoGeneral estado;
    @NotNull
    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;
    @NotNull
    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;
}
