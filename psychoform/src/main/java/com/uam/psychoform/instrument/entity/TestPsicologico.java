package com.uam.psychoform.instrument.entity;

import com.uam.psychoform.security.entity.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;
import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "test")
public class TestPsicologico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "test_id")
    private Long id;
    @NotBlank
    @Size(max = 50)
    @Column(name = "codigo_test", nullable = false, length = 50)
    private String codigoTest;
    @NotBlank
    @Size(max = 150)
    @Column(name = "nombre_test", nullable = false, length = 150)
    private String nombreTest;
    @Column(columnDefinition = "text")
    private String descripcion;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por")
    private Usuario creadoPor;
    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "estado_general")
    private EstadoGeneral estado;
    @NotNull
    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;
}
