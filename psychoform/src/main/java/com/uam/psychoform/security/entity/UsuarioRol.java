package com.uam.psychoform.security.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuario_rol")
public class UsuarioRol {
    @EmbeddedId
    private UsuarioRolId id;
}
