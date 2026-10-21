package com.baghaztra.tailorworkflow.controller;

import com.baghaztra.tailorworkflow.entity.Worker;
import com.baghaztra.tailorworkflow.repository.WorkerRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/workers")
public class WorkerController {

    private final WorkerRepository workers;

    public WorkerController(WorkerRepository workers) {
        this.workers = workers;
    }

    @GetMapping
    public List<WorkerResponse> list(@RequestParam(required = false) Boolean active) {
        List<Worker> result = active == null ? workers.findAll() : workers.findByActive(active);
        return result.stream().map(WorkerResponse::from).toList();
    }

    @GetMapping("/{id}")
    public WorkerResponse get(@PathVariable Long id) {
        return WorkerResponse.from(find(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkerResponse create(@Valid @RequestBody SaveWorkerRequest request) {
        Worker worker = new Worker(request.name(), request.specialization());
        if (request.active() != null) {
            worker.setActive(request.active());
        }
        return WorkerResponse.from(workers.save(worker));
    }

    @PutMapping("/{id}")
    public WorkerResponse update(@PathVariable Long id, @Valid @RequestBody SaveWorkerRequest request) {
        Worker worker = find(id);
        worker.setName(request.name());
        worker.setSpecialization(request.specialization());
        if (request.active() != null) {
            worker.setActive(request.active());
        }
        return WorkerResponse.from(workers.save(worker));
    }

    private Worker find(Long id) {
        return workers.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Worker " + id + " not found"));
    }

    public record SaveWorkerRequest(
            @NotBlank @Size(max = 100) String name,
            @NotNull Worker.Specialization specialization,
            Boolean active) { }

    public record WorkerResponse(
            Long id,
            String name,
            Worker.Specialization specialization,
            boolean active,
            Instant createdAt) {

        static WorkerResponse from(Worker worker) {
            return new WorkerResponse(
                    worker.getId(),
                    worker.getName(),
                    worker.getSpecialization(),
                    worker.isActive(),
                    worker.getCreatedAt());
        }
    }
}
