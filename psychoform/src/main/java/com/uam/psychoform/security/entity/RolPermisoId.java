package com.uam.psychoform.security.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class RolPermisoId implements Serializable {
    @Column(name = "rol_id")
    private Short rolId;

    @Column(name = "permiso_id")
    private Short permisoId;

    public RolPermisoId() { }

    public RolPermisoId(Short rolId, Short permisoId) {
        this.rolId = rolId;
        this.permisoId = permisoId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof RolPermisoId that)) {
            return false;
        }
        return Objects.equals(rolId, that.rolId) && Objects.equals(permisoId, that.permisoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rolId, permisoId);
    }
}
