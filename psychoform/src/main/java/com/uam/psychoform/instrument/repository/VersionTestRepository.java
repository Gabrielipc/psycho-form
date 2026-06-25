package com.uam.psychoform.instrument.repository;

import com.uam.psychoform.instrument.model.VersionTest;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;

public interface VersionTestRepository extends JpaRepository<VersionTest, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from VersionTest v where v.id = :id")
    Optional<VersionTest> findByIdForUpdate(Long id);
}
