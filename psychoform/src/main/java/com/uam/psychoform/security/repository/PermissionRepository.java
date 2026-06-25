package com.uam.psychoform.security.repository;

import com.uam.psychoform.security.entity.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permiso, Short> { }
