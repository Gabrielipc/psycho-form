package com.uam.psychoform.security.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class UsuarioRolId implements Serializable {
    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(name = "rol_id")
    private Short rolId;

    public UsuarioRolId() { }

    public UsuarioRolId(UUID usuarioId, Short rolId) {
        this.usuarioId = usuarioId;
        this.rolId = rolId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof UsuarioRolId that)) {
            return false;
        }
        return Objects.equals(usuarioId, that.usuarioId) && Objects.equals(rolId, that.rolId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usuarioId, rolId);
    }
}
