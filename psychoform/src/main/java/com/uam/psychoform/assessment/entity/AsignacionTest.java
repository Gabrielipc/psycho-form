package com.uam.psychoform.assessment.entity;

import com.uam.psychoform.academic.entity.*;
import com.uam.psychoform.security.entity.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;
import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "asignacion_test")
public class AsignacionTest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "asignacion_id")
    private Long id;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sesion_aplicacion_id", nullable = false)
    private SesionAplicacion sesionAplicacion;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participante_id", nullable = false)
    private Participante participante;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluador_id", nullable = false)
    private Usuario evaluador;
    @NotBlank
    @Size(max = 255)
    @Column(name = "token_acceso_hash", nullable = false, length = 255)
    private String tokenAccesoHash;
    @NotNull
    @Column(name = "token_expira_en", nullable = false)
    private LocalDateTime tokenExpiraEn;
    @Column(name = "token_usado_en")
    private LocalDateTime tokenUsadoEn;
    @NotNull
    @Min(0)
    private Integer intentosAcceso;
    @Min(0)
    @Max(120)
    private Short edadRegistradaAplicacion;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sexo_id_aplicacion")
    private CatalogoSexo sexoAplicacion;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrera_id_aplicacion")
    private Carrera carreraAplicacion;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cohorte_id_aplicacion")
    private Cohorte cohorteAplicacion;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_academico_id_aplicacion")
    private GrupoAcademico grupoAcademicoAplicacion;
    @NotNull
    @Column(name = "asignado_en", nullable = false)
    private LocalDateTime asignadoEn;
    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "estado_asignacion")
    private EstadoAsignacion estado;
}
