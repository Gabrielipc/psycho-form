package com.uam.psychoform.scoring.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "revision_manual_respuesta")
public class RevisionManualRespuesta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "revision_manual_respuesta_id", nullable = false)
    private Long id;
}
