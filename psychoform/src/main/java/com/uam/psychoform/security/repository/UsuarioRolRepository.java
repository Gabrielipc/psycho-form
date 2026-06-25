package com.uam.psychoform.security.repository;

import com.uam.psychoform.security.model.UsuarioRol;
import com.uam.psychoform.security.model.UsuarioRolId;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UsuarioRolRepository extends JpaRepository<UsuarioRol, UsuarioRolId> {
    @Query("select distinct usuarioRol.rol.nombre from UsuarioRol usuarioRol where usuarioRol.usuario.id = :usuarioId")
    Set<String> findRoleNamesByUsuarioId(UUID usuarioId);
}
