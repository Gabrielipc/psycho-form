package com.uam.psychoform.assessment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sesion_subtest")
public class SesionSubtest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sesion_subtest_id", nullable = false)
    private Long id;
}
