package com.uam.psychoform.assessment.entity;

import lombok.Getter;
import lombok.Setter;

import com.uam.psychoform.instrument.entity.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Getter
@Setter
@Entity
@Table(name = "sesion_subtest")
public class SesionSubtest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sesion_subtest_id")
    private Long id;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sesion_aplicacion_id", nullable = false)
    private SesionAplicacion sesionAplicacion;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_test_id", nullable = false)
    private VersionTest versionTest;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subtest_id", nullable = false)
    private Subtest subtest;
    @NotNull
    private Integer numeroOrden;
    @Positive
    private Integer tiempoLimiteSegundos;
    @NotNull
    private Boolean permiteAleatorizarItems;
    @NotNull
    private Boolean permiteAleatorizarOpciones;
}
