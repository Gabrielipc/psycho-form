package com.uam.psychoform.scoring.entity;

import com.uam.psychoform.assessment.entity.*;
import com.uam.psychoform.instrument.entity.*;
import com.uam.psychoform.security.entity.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.*;
import java.time.*;
import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "revision_manual_respuesta")
public class RevisionManualRespuesta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "revision_manual_respuesta_id")
    private Long id;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "respuesta_id", nullable = false)
    private RespuestaItem respuesta;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regla_calificacion_id")
    private ReglaCalificacion reglaCalificacion;
    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "estado_revision_manual")
    private EstadoRevisionManual estado;
    @DecimalMin("0")
    @Column(name = "puntaje_asignado", precision = 10, scale = 2)
    private BigDecimal puntajeAsignado;
    @Column(columnDefinition = "text")
    private String comentario;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "revisado_por")
    private Usuario revisadoPor;
    @Column(name = "revisado_en")
    private LocalDateTime revisadoEn;
    @NotNull
    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;
}
