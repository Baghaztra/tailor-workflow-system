package com.baghaztra.tailorworkflow.service;

import com.baghaztra.tailorworkflow.entity.Worker;
import com.baghaztra.tailorworkflow.repository.WorkerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class WorkerService {

    private final WorkerRepository workers;

    public WorkerService(WorkerRepository workers) {
        this.workers = workers;
    }

    @Transactional(readOnly = true)
    public Page<Worker> list(Boolean active, String search, int page, int size) {
        Specification<Worker> spec = (root, query, cb) -> cb.conjunction();
        if (active != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("active"), active));
        }
        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("name")), pattern));
        }

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.ASC, "name"));
        return workers.findAll(spec, pageable);
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
