package com.uam.psychoform.assessment.entity;

import com.uam.psychoform.instrument.entity.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;
import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "intento_subtest")
public class IntentoSubtest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "intento_subtest_id")
    private Long id;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intento_id", nullable = false)
    private IntentoTest intento;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sesion_subtest_id", nullable = false)
    private SesionSubtest sesionSubtest;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subtest_id", nullable = false)
    private Subtest subtest;
    @Column(name = "iniciado_en")
    private LocalDateTime iniciadoEn;
    @Column(name = "finalizado_en")
    private LocalDateTime finalizadoEn;
    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "estado_intento")
    private EstadoIntento estado;
    @Min(0)
    private Integer tiempoUsadoSegundos;
}
