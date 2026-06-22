package com.uam.psychoform.instrument.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "opcion_puntaje_dimension")
public class OpcionPuntajeDimension {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "opcion_puntaje_dimension_id", nullable = false)
    private Long id;
}
