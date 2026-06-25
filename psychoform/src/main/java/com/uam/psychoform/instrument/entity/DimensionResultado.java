package com.uam.psychoform.instrument.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.*;
import java.time.*;

@Getter
@Setter
@Entity
@Table(name = "dimension_resultado")
public class DimensionResultado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dimension_resultado_id")
    private Long id;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_test_id", nullable = false)
    private VersionTest versionTest;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subtest_id")
    private Subtest subtest;
    @NotBlank
    @Size(max = 60)
    @Column(name = "codigo_dimension", nullable = false, length = 60)
    private String codigoDimension;
    @NotBlank
    @Size(max = 150)
    @Column(name = "nombre_dimension", nullable = false, length = 150)
    private String nombreDimension;
    @Column(columnDefinition = "text")
    private String descripcion;
    @NotNull
    private Integer ordenPresentacion;
    @Column(precision = 10, scale = 2)
    private BigDecimal puntajeMinimo;
    @Column(precision = 10, scale = 2)
    private BigDecimal puntajeMaximo;
    @NotNull
    private Boolean activa;
    @NotNull
    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;
}
