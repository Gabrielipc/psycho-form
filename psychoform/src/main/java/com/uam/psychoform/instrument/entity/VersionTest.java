package com.uam.psychoform.instrument.entity;

import com.uam.psychoform.security.entity.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;
import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "version_test", uniqueConstraints = @UniqueConstraint(columnNames = { "test_id", "numero_version" }))
public class VersionTest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "version_test_id")
    private Long id;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private TestPsicologico test;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estrategia_calificacion_id")
    private EstrategiaCalificacion estrategiaCalificacion;
    @NotBlank
    @Size(max = 30)
    @Column(name = "numero_version", nullable = false, length = 30)
    private String numeroVersion;
    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "estado_version_test")
    private EstadoVersionTest estado;
    @Column(name = "instrucciones_generales", columnDefinition = "text")
    private String instruccionesGenerales;
    @Positive
    private Integer tiempoLimiteSegundos;
    @NotNull
    private Boolean permiteAleatorizarSubtests;
    @NotNull
    private Boolean permiteAleatorizarItems;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aprobado_por")
    private Usuario aprobadoPor;
    @Column(name = "aprobado_en")
    private LocalDateTime aprobadoEn;
    @Column(name = "publicado_en")
    private LocalDateTime publicadoEn;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por")
    private Usuario creadoPor;
    @NotNull
    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;
}
