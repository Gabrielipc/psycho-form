package com.uam.psychoform.instrument.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "imagen_opcion")
public class ImagenOpcion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "imagen_opcion_id", nullable = false)
    private Long id;
}
