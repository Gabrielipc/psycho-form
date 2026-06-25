package com.uam.psychoform.instrument.model;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.*;

@Getter
@Setter
@Entity
@Table(name = "rango_baremo")
public class RangoBaremo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rango_baremo_id")
    private Long id;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "baremo_id", nullable = false)
    private Baremo baremo;
    @NotNull
    @Column(name = "puntaje_minimo", nullable = false, precision = 10, scale = 2)
    private BigDecimal puntajeMinimo;
    @NotNull
    @Column(name = "puntaje_maximo", nullable = false, precision = 10, scale = 2)
    private BigDecimal puntajeMaximo;
    @DecimalMin("0")
    @DecimalMax("100")
    @Column(precision = 6, scale = 2)
    private BigDecimal percentil;
    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String categoria;
    @Column(columnDefinition = "text")
    private String interpretacion;
    @Column(columnDefinition = "text")
    private String recomendacion;
    @NotNull
    private Integer orden;
}
