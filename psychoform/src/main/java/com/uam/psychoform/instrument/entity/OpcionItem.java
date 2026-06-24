package com.uam.psychoform.instrument.entity;

import com.uam.psychoform.security.entity.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.*;
import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "opcion_item")
public class OpcionItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "opcion_id")
    private Long id;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;
    @NotBlank
    @Size(max = 50)
    @Column(name = "codigo_opcion", nullable = false, length = 50)
    private String codigoOpcion;
    @Column(name = "texto_opcion", columnDefinition = "text")
    private String textoOpcion;
    @NotNull
    private Integer numeroOrden;
    @Column(name = "valor_ordinal", precision = 8, scale = 2)
    private BigDecimal valorOrdinal;
    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "estado_general")
    private EstadoGeneral estado;
}
