package com.uam.psychoform.security.repository;

import com.uam.psychoform.security.model.Permiso;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permiso, Short> {
    List<Permiso> findAllByOrderByCodigoAsc();
}
