package com.baghaztra.tailorworkflow.controller;

import com.baghaztra.tailorworkflow.dto.PageResponse;
import com.baghaztra.tailorworkflow.entity.Worker;
import com.baghaztra.tailorworkflow.service.WorkerService;
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

import java.time.Instant;

@RestController
@RequestMapping("/api/workers")
public class WorkerController {

    private final WorkerService workers;

    public WorkerController(WorkerService workers) {
        this.workers = workers;
    }

    @GetMapping
    public PageResponse<WorkerResponse> list(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return PageResponse.from(workers.list(active, search, page, size), WorkerResponse::from);
    }

    @GetMapping("/{id}")
    public WorkerResponse get(@PathVariable Long id) {
        return WorkerResponse.from(workers.get(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkerResponse create(@Valid @RequestBody SaveWorkerRequest request) {
        return WorkerResponse.from(workers.create(request.name(), request.specialization(), request.active()));
    }

    @PutMapping("/{id}")
    public WorkerResponse update(@PathVariable Long id, @Valid @RequestBody SaveWorkerRequest request) {
        return WorkerResponse.from(workers.update(id, request.name(), request.specialization(), request.active()));
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
