package com.uam.psychoform.instrument.entity;

import com.uam.psychoform.security.entity.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.*;
import java.time.*;

@Entity
@Table(name = "clave_respuesta")
public class ClaveRespuesta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "clave_respuesta_id")
    private Long id;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regla_calificacion_id", nullable = false)
    private ReglaCalificacion reglaCalificacion;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opcion_correcta_id")
    private OpcionItem opcionCorrecta;
    @Column(name = "texto_esperado", columnDefinition = "text")
    private String textoEsperado;
    @Column(name = "valor_numerico_esperado", precision = 12, scale = 4)
    private BigDecimal valorNumericoEsperado;
    @DecimalMin("0")
    @Column(name = "tolerancia_numerica", precision = 12, scale = 4)
    private BigDecimal toleranciaNumerica;
    @NotNull
    @DecimalMin("0")
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal puntaje;
    @NotNull
    private Boolean requiereRevisionManual;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por")
    private Usuario creadoPor;
    @NotNull
    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;
}
