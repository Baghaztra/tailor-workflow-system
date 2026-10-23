package com.baghaztra.tailorworkflow.repository;

import com.baghaztra.tailorworkflow.entity.Worker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface WorkerRepository extends JpaRepository<Worker, Long>, JpaSpecificationExecutor<Worker> {

    long countByActiveTrue();
}
