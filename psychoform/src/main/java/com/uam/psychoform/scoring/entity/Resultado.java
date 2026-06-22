package com.uam.psychoform.scoring.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "resultado")
public class Resultado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resultado_id", nullable = false)
    private Long id;
}
