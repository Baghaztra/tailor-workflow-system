package com.baghaztra.tailorworkflow.repository;

import com.baghaztra.tailorworkflow.entity.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

    long countByAttemptKeyAndAttemptedAtAfter(String attemptKey, Instant cutoff);

    Optional<LoginAttempt> findFirstByAttemptKeyAndAttemptedAtAfterOrderByAttemptedAtAsc(
            String attemptKey,
            Instant cutoff);

    void deleteByAttemptKey(String attemptKey);

    void deleteByAttemptedAtBefore(Instant cutoff);
}
