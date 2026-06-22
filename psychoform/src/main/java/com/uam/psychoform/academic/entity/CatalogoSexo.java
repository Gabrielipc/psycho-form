package com.uam.psychoform.academic.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "catalogo_sexo")
public class CatalogoSexo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sexo_id", nullable = false)
    private Short id;
}
