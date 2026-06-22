package com.uam.psychoform.instrument.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "criterio_rubrica")
public class CriterioRubrica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "criterio_rubrica_id", nullable = false)
    private Long id;
}
