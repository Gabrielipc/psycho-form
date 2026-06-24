package com.uam.psychoform.academic.entity;

import com.uam.psychoform.security.entity.EstadoGeneral;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "cohorte")
@Getter
@Setter
public class Cohorte implements CatalogoEntidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cohorte_id")
    private Short id;
    @NotBlank
    @Size(max = 30)
    @Column(name = "codigo_cohorte", nullable = false, length = 30)
    private String codigoCohorte;
    @NotBlank
    @Size(max = 100)
    @Column(name = "nombre_cohorte", nullable = false, length = 100)
    private String nombreCohorte;
    @Min(1900)
    @Max(2100)
    private Short anio;
    @Size(max = 30)
    @Column(length = 30)
    private String periodo;
    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "estado_general")
    private EstadoGeneral estado;
}
