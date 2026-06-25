package com.uam.psychoform.scoring.model;

import lombok.Getter;
import lombok.Setter;

import com.uam.psychoform.assessment.model.*;
import com.uam.psychoform.instrument.model.*;
import com.uam.psychoform.security.model.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.*;
import java.time.*;

@Getter
@Setter
@Entity
@Table(name = "resultado")
public class Resultado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resultado_id")
    private Long id;
    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intento_id", nullable = false)
    private IntentoTest intento;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estrategia_calificacion_id")
    private EstrategiaCalificacion estrategiaCalificacion;
    @NotNull
    @Column(name = "calculado_en", nullable = false)
    private LocalDateTime calculadoEn;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calculado_por")
    private Usuario calculadoPor;
    @NotNull
    @Column(name = "puntaje_total_directo", nullable = false, precision = 10, scale = 2)
    private BigDecimal puntajeTotalDirecto;
    @NotNull
    @Min(0)
    private Integer cantidadItems;
    @NotNull
    @Min(0)
    private Integer cantidadCorrectas;
    @NotNull
    @Min(0)
    private Integer cantidadIncorrectas;
    @NotNull
    @Min(0)
    private Integer cantidadPendientesRevision;
    @NotNull
    private Boolean requiereRevisionManual;
    @NotBlank
    @Size(max = 30)
    @Column(nullable = false, length = 30)
    private String estado;
}
