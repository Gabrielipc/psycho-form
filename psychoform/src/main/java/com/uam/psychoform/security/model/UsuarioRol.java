package com.uam.psychoform.security.model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuario_rol")
public class UsuarioRol {
    @EmbeddedId
    private UsuarioRolId id;
    @MapsId("usuarioId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
    @MapsId("rolId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rol_id")
    private Rol rol;

    public UsuarioRol() { }

    public UsuarioRol(Usuario usuario, Rol rol) {
        this.id = new UsuarioRolId(usuario.getId(), rol.getId());
        this.usuario = usuario;
        this.rol = rol;
    }
}
