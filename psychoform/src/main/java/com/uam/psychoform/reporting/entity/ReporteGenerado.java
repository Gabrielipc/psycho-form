package com.uam.psychoform.reporting.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "reporte_generado")
public class ReporteGenerado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reporte_id", nullable = false)
    private Long id;
}
