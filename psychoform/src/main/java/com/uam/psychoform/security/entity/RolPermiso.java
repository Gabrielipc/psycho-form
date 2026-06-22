package com.uam.psychoform.security.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "rol_permiso")
public class RolPermiso {
    @EmbeddedId
    private RolPermisoId id;
}
