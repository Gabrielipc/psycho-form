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
@Table(name = "revision_rubrica_respuesta")
public class RevisionRubricaRespuesta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "revision_rubrica_respuesta_id")
    private Long id;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "revision_manual_respuesta_id", nullable = false)
    private RevisionManualRespuesta revisionManualRespuesta;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criterio_rubrica_id", nullable = false)
    private CriterioRubrica criterioRubrica;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nivel_criterio_rubrica_id")
    private NivelCriterioRubrica nivelCriterioRubrica;
    @NotNull
    @DecimalMin("0")
    @Column(name = "puntaje_asignado", nullable = false, precision = 10, scale = 2)
    private BigDecimal puntajeAsignado;
    @Column(columnDefinition = "text")
    private String comentario;
}
