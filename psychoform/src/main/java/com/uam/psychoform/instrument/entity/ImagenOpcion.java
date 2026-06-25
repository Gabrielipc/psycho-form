package com.uam.psychoform.instrument.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Getter
@Setter
@Entity
@Table(name = "imagen_opcion")
public class ImagenOpcion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "imagen_opcion_id")
    private Long id;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opcion_id", nullable = false)
    private OpcionItem opcion;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recurso_id", nullable = false)
    private RecursoMultimedia recurso;
    @NotNull
    private Integer numeroOrden;
    @Size(max = 255)
    @Column(name = "texto_alternativo", length = 255)
    private String textoAlternativo;
}
