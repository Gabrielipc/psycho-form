package com.uam.psychoform.instrument.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "rango_baremo")
public class RangoBaremo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rango_baremo_id", nullable = false)
    private Long id;
}
