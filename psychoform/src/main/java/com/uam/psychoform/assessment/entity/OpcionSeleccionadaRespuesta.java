package com.uam.psychoform.assessment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "opcion_seleccionada_respuesta")
public class OpcionSeleccionadaRespuesta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "respuesta_id", nullable = false)
    private Long id;
}
