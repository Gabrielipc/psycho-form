package com.uam.psychoform.instrument.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Getter
@Setter
@Entity
@Table(name = "imagen_item")
public class ImagenItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "imagen_item_id")
    private Long id;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recurso_id", nullable = false)
    private RecursoMultimedia recurso;
    @NotBlank
    @Size(max = 50)
    @Column(name = "rol_imagen", nullable = false, length = 50)
    private String rolImagen;
    @NotNull
    private Integer numeroOrden;
    @Size(max = 255)
    @Column(name = "texto_alternativo", length = 255)
    private String textoAlternativo;
}
