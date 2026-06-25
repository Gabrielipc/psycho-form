package com.uam.psychoform.instrument.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.*;

@Getter
@Setter
@Entity
@Table(name = "nivel_criterio_rubrica")
public class NivelCriterioRubrica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nivel_criterio_rubrica_id")
    private Long id;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criterio_rubrica_id", nullable = false)
    private CriterioRubrica criterioRubrica;
    @NotBlank
    @Size(max = 60)
    @Column(name = "codigo_nivel", nullable = false, length = 60)
    private String codigoNivel;
    @NotBlank
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String nombre;
    @Column(columnDefinition = "text")
    private String descripcion;
    @NotNull
    @DecimalMin("0")
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal puntaje;
    @NotNull
    private Integer orden;
}
