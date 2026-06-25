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
@Table(name = "opcion_puntaje_dimension")
public class OpcionPuntajeDimension {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "opcion_puntaje_dimension_id")
    private Long id;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regla_calificacion_id", nullable = false)
    private ReglaCalificacion reglaCalificacion;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opcion_id", nullable = false)
    private OpcionItem opcion;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dimension_resultado_id", nullable = false)
    private DimensionResultado dimensionResultado;
    @NotNull
    @DecimalMin("0")
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal puntaje;
    @NotNull
    @Positive
    @Column(nullable = false, precision = 8, scale = 4)
    private BigDecimal peso;
    @NotNull
    private Boolean activa;
    @NotNull
    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;
}
