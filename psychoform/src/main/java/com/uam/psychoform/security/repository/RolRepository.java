package com.uam.psychoform.security.repository;

import com.uam.psychoform.security.model.Rol;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolRepository extends JpaRepository<Rol, Short> {
    Optional<Rol> findByNombre(String nombre);
}
