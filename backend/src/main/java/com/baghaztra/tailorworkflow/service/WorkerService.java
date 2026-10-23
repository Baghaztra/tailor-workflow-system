package com.baghaztra.tailorworkflow.service;

import com.baghaztra.tailorworkflow.entity.Worker;
import com.baghaztra.tailorworkflow.repository.WorkerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class WorkerService {

    private final WorkerRepository workers;

    public WorkerService(WorkerRepository workers) {
        this.workers = workers;
    }

    @Transactional(readOnly = true)
    public List<Worker> list(Boolean active) {
        return active == null ? workers.findAll() : workers.findByActive(active);
    }

    @Transactional(readOnly = true)
    public Worker get(Long id) {
        return find(id);
    }

    @Transactional
    public Worker create(String name, Worker.Specialization specialization, Boolean active) {
        Worker worker = new Worker(name, specialization);
        if (active != null) {
            worker.setActive(active);
        }
        return workers.save(worker);
    }

    @Transactional
    public Worker update(Long id, String name, Worker.Specialization specialization, Boolean active) {
        Worker worker = find(id);
        worker.setName(name);
        worker.setSpecialization(specialization);
        if (active != null) {
            worker.setActive(active);
        }
        return workers.save(worker);
    }

    private Worker find(Long id) {
        return workers.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Worker " + id + " not found"));
    }
}
