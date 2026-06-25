package com.uam.psychoform.instrument.entity;

import lombok.Getter;
import lombok.Setter;

import com.uam.psychoform.security.entity.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.*;
import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "item")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subtest_id", nullable = false)
    private Subtest subtest;
    @NotBlank
    @Size(max = 80)
    @Column(name = "codigo_item", nullable = false, length = 80)
    private String codigoItem;
    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "tipo_item", nullable = false, columnDefinition = "tipo_item")
    private TipoItem tipoItem;
    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "tipo_respuesta", nullable = false, columnDefinition = "tipo_respuesta")
    private TipoRespuesta tipoRespuesta;
    @Column(columnDefinition = "text")
    private String enunciado;
    @Column(columnDefinition = "text")
    private String instruccion;
    @NotNull
    private Integer numeroOrden;
    @NotNull
    @DecimalMin("0")
    @Column(name = "puntaje_base", nullable = false, precision = 8, scale = 2)
    private BigDecimal puntajeBase;
    @Positive
    private Integer tiempoLimiteSegundos;
    @NotNull
    private Boolean esObligatorio;
    @NotNull
    private Boolean esConfidencial;
    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "estado_general")
    private EstadoGeneral estado;
}
