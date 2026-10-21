package com.baghaztra.tailorworkflow.repository;

import com.baghaztra.tailorworkflow.entity.Worker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkerRepository extends JpaRepository<Worker, Long> {

    List<Worker> findByActive(boolean active);
}
