package com.uam.psychoform.scoring.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "revision_rubrica_respuesta")
public class RevisionRubricaRespuesta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "revision_rubrica_respuesta_id", nullable = false)
    private Long id;
}
