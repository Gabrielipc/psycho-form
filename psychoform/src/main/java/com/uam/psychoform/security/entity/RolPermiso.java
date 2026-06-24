package com.uam.psychoform.security.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "rol_permiso")
public class RolPermiso {
    @EmbeddedId
    private RolPermisoId id;
    @MapsId("rolId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rol_id")
    private Rol rol;
    @MapsId("permisoId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permiso_id")
    private Permiso permiso;
}
