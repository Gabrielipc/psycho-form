package com.uam.psychoform.instrument.entity;

import com.uam.psychoform.academic.entity.*;
import com.uam.psychoform.security.entity.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;
import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "baremo")
public class Baremo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "baremo_id")
    private Long id;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_test_id", nullable = false)
    private VersionTest versionTest;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dimension_resultado_id")
    private DimensionResultado dimensionResultado;
    @NotBlank
    @Size(max = 60)
    @Column(name = "codigo_baremo", nullable = false, length = 60)
    private String codigoBaremo;
    @NotBlank
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String nombre;
    @Column(columnDefinition = "text")
    private String descripcion;
    @Size(max = 150)
    private String grupoNormativo;
    @Min(0)
    @Max(120)
    private Short criterioEdadMinima;
    @Min(0)
    @Max(120)
    private Short criterioEdadMaxima;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criterio_sexo_id")
    private CatalogoSexo criterioSexo;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criterio_carrera_id")
    private Carrera criterioCarrera;
    @Column(name = "vigente_desde")
    private LocalDate vigenteDesde;
    @Column(name = "vigente_hasta")
    private LocalDate vigenteHasta;
    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "estado_configuracion")
    private EstadoConfiguracion estado;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por")
    private Usuario creadoPor;
    @NotNull
    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aprobado_por")
    private Usuario aprobadoPor;
    @Column(name = "aprobado_en")
    private LocalDateTime aprobadoEn;
}
