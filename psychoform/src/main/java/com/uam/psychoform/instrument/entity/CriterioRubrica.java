package com.uam.psychoform.instrument.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.*;

@Entity
@Table(name = "criterio_rubrica")
public class CriterioRubrica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "criterio_rubrica_id")
    private Long id;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rubrica_id", nullable = false)
    private RubricaEvaluacion rubrica;
    @NotBlank
    @Size(max = 60)
    @Column(name = "codigo_criterio", nullable = false, length = 60)
    private String codigoCriterio;
    @NotBlank
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String nombre;
    @Column(columnDefinition = "text")
    private String descripcion;
    @NotNull
    @DecimalMin("0")
    @Column(name = "puntaje_maximo", nullable = false, precision = 8, scale = 2)
    private BigDecimal puntajeMaximo;
    @NotNull
    @Positive
    @Column(precision = 8, scale = 4)
    private BigDecimal peso;
    @NotNull
    private Integer orden;
}
