package com.uam.psychoform.academic.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "participante")
public class Participante {
    @Id
    @Column(name = "participante_id", nullable = false)
    private UUID id;
}
