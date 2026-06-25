package com.uam.psychoform.instrument.model;

import lombok.Getter;
import lombok.Setter;

import com.uam.psychoform.security.model.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "subtest")
public class Subtest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subtest_id")
    private Long id;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_test_id", nullable = false)
    private VersionTest versionTest;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estrategia_calificacion_id")
    private EstrategiaCalificacion estrategiaCalificacion;
    @NotBlank
    @Size(max = 50)
    @Column(name = "codigo_subtest", nullable = false, length = 50)
    private String codigoSubtest;
    @NotBlank
    @Size(max = 150)
    @Column(name = "nombre_subtest", nullable = false, length = 150)
    private String nombreSubtest;
    @Column(columnDefinition = "text")
    private String descripcion;
    @Column(columnDefinition = "text")
    private String instrucciones;
    @NotNull
    private Integer numeroOrden;
    @Positive
    private Integer tiempoLimiteSegundos;
    @NotNull
    private Boolean permiteAleatorizarItems;
    @NotNull
    private Boolean permiteAleatorizarOpciones;
    @NotNull
    private Boolean esObligatorio;
    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "estado_general")
    private EstadoGeneral estado;
}
