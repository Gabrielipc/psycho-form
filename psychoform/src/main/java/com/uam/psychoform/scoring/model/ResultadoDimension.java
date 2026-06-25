package com.uam.psychoform.scoring.model;

import lombok.Getter;
import lombok.Setter;

import com.uam.psychoform.instrument.model.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.*;

@Getter
@Setter
@Entity
@Table(name = "resultado_dimension")
public class ResultadoDimension {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resultado_dimension_id")
    private Long id;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resultado_id", nullable = false)
    private Resultado resultado;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dimension_resultado_id", nullable = false)
    private DimensionResultado dimensionResultado;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "baremo_id")
    private Baremo baremo;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rango_baremo_id")
    private RangoBaremo rangoBaremo;
    @NotNull
    @Column(name = "puntaje_directo", nullable = false, precision = 10, scale = 2)
    private BigDecimal puntajeDirecto;
    @Column(name = "puntaje_transformado", precision = 10, scale = 2)
    private BigDecimal puntajeTransformado;
    @DecimalMin("0")
    @DecimalMax("100")
    private BigDecimal percentil;
    @Size(max = 100)
    private String categoria;
    @Column(columnDefinition = "text")
    private String interpretacion;
    @NotNull
    private Boolean requiereRevisionManual;
    @Column(columnDefinition = "text")
    private String observacion;
}
