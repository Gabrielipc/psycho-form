package com.uam.psychoform.assessment.entity;

import lombok.Getter;
import lombok.Setter;

import com.uam.psychoform.instrument.entity.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.*;
import java.time.*;

@Getter
@Setter
@Entity
@Table(name = "respuesta_item")
public class RespuestaItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "respuesta_id")
    private Long id;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intento_id", nullable = false)
    private IntentoTest intento;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intento_subtest_id")
    private IntentoSubtest intentoSubtest;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;
    @Column(name = "respuesta_texto_abierto", columnDefinition = "text")
    private String respuestaTextoAbierto;
    @Column(name = "respuesta_numerica", precision = 12, scale = 4)
    private BigDecimal respuestaNumerica;
    @NotNull
    @Column(name = "respondido_en", nullable = false)
    private LocalDateTime respondidoEn;
    @Min(0)
    private Integer tiempoUsadoSegundos;
    @NotNull
    private Boolean esFinal;
    @NotNull
    private Boolean requiereRevisionManual;
}
