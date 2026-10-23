package com.baghaztra.tailorworkflow.repository;

import com.baghaztra.tailorworkflow.entity.AuthSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface AuthSessionRepository extends JpaRepository<AuthSession, String> {

    void deleteByExpiresAtBefore(Instant cutoff);
}
