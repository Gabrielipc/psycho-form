package com.uam.psychoform.instrument.model;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;
import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "estrategia_calificacion")
public class EstrategiaCalificacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "estrategia_calificacion_id")
    private Short id;
    @NotBlank
    @Size(max = 60)
    @Column(nullable = false, length = 60)
    private String codigo;
    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String nombre;
    @Column(columnDefinition = "text")
    private String descripcion;
    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "tipo_estrategia", nullable = false, columnDefinition = "tipo_estrategia_calificacion")
    private TipoEstrategiaCalificacion tipoEstrategia;
    @NotNull
    private Boolean activa;
    @NotNull
    private Boolean requiereRevisionManual;
    @NotNull
    private Boolean permiteBaremo;
    @NotNull
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
}
