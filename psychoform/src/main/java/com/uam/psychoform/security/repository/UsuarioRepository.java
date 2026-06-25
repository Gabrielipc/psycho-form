package com.uam.psychoform.security.repository;
import com.uam.psychoform.security.model.Usuario;
import com.uam.psychoform.security.model.EstadoGeneral;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    Optional<Usuario> findByNombreUsuario(String nombreUsuario);
    Optional<Usuario> findByNombreUsuarioAndEstado(String nombreUsuario, EstadoGeneral estado);
}
