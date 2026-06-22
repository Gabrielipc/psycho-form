package com.uam.psychoform.security.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "rol")
public class Rol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rol_id")
    private Short id;

    @Column(name = "nombre_rol", nullable = false, length = 60)
    private String nombre;

    @Column(length = 255)
    private String descripcion;
}
