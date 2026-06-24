package com.uam.psychoform.assessment.entity;

import com.uam.psychoform.instrument.entity.*;
import com.uam.psychoform.security.entity.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;
import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "sesion_aplicacion")
public class SesionAplicacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sesion_aplicacion_id")
    private Long id;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_test_id", nullable = false)
    private VersionTest versionTest;
    @NotBlank
    @Size(max = 80)
    @Column(name = "codigo_sesion", nullable = false, length = 80)
    private String codigoSesion;
    @NotBlank
    @Size(max = 150)
    @Column(name = "nombre_sesion", nullable = false, length = 150)
    private String nombreSesion;
    @Column(columnDefinition = "text")
    private String descripcion;
    @NotNull
    @Column(name = "inicio_programado", nullable = false)
    private LocalDateTime inicioProgramado;
    @Column(name = "fin_programado")
    private LocalDateTime finProgramado;
    @Column(name = "inicio_real")
    private LocalDateTime inicioReal;
    @Column(name = "fin_real")
    private LocalDateTime finReal;
    @Size(max = 150)
    private String ubicacion;
    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "estado_sesion_aplicacion")
    private EstadoSesionAplicacion estado;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por")
    private Usuario creadoPor;
    @NotNull
    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;
}
