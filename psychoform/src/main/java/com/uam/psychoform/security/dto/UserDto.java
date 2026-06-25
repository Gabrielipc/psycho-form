package com.uam.psychoform.security.dto;

import com.uam.psychoform.security.model.EstadoGeneral;
import com.uam.psychoform.security.model.Usuario;
import java.util.UUID;

public record UserDto(UUID id, String username, String email, String fullName, EstadoGeneral status) {
    public static UserDto from(Usuario user) {
        return new UserDto(user.getId(), user.getNombreUsuario(), user.getCorreo(), user.getNombreCompleto(),
                user.getEstado());
    }
}