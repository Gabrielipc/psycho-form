package com.uam.psychoform.scoring.entity;

import com.uam.psychoform.assessment.entity.*;
import com.uam.psychoform.instrument.entity.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.*;
import java.time.*;

@Entity
@Table(name = "calificacion_respuesta")
public class CalificacionRespuesta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "calificacion_respuesta_id")
    private Long id;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resultado_id", nullable = false)
    private Resultado resultado;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "respuesta_id", nullable = false)
    private RespuestaItem respuesta;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regla_calificacion_id")
    private ReglaCalificacion reglaCalificacion;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dimension_resultado_id")
    private DimensionResultado dimensionResultado;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opcion_id")
    private OpcionItem opcion;
    @NotNull
    @Column(name = "puntaje_obtenido", nullable = false, precision = 10, scale = 2)
    private BigDecimal puntajeObtenido;
    private Boolean esCorrecta;
    @NotNull
    private Boolean requiereRevisionManual;
    @Column(columnDefinition = "text")
    private String observacion;
    @NotNull
    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;
}
