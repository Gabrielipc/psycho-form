package com.uam.psychoform.assessment.model;

import lombok.Getter;
import lombok.Setter;

import com.uam.psychoform.instrument.model.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;
import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "intento_test")
public class IntentoTest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "intento_id")
    private Long id;
    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asignacion_id", nullable = false)
    private AsignacionTest asignacion;
    @Column(name = "iniciado_en")
    private LocalDateTime iniciadoEn;
    @Column(name = "finalizado_en")
    private LocalDateTime finalizadoEn;
    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "estado_intento")
    private EstadoIntento estado;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ultimo_subtest_id")
    private Subtest ultimoSubtest;
    @Column(name = "ultima_actividad_en")
    private LocalDateTime ultimaActividadEn;
    @Min(0)
    private Integer tiempoTotalSegundos;
    @Column(name = "informacion_dispositivo", columnDefinition = "text")
    private String informacionDispositivo;
    @Size(max = 60)
    @Column(name = "direccion_ip", length = 60)
    private String direccionIp;
}
