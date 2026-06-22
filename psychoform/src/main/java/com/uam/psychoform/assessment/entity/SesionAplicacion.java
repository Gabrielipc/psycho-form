package com.uam.psychoform.assessment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sesion_aplicacion")
public class SesionAplicacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sesion_aplicacion_id", nullable = false)
    private Long id;
}
