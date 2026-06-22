package com.uam.psychoform.instrument.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "regla_calificacion")
public class ReglaCalificacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "regla_calificacion_id", nullable = false)
    private Long id;
}
