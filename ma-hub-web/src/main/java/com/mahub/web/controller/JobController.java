package com.mahub.web.controller;

import com.mahub.core.domain.enums.JobStatus;
import com.mahub.core.dto.GenerationJobResponse;
import com.mahub.core.dto.GenerationRequest;
import com.mahub.core.service.GenerationJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Objects;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class JobController {

    private final GenerationJobService generationJobService;

    @PostMapping("/generate")
    public ResponseEntity<GenerationJobResponse> createJob(
            @RequestBody GenerationRequest request) {
        Objects.requireNonNull(request, "request");
        if (request.definitionIds() == null || request.definitionIds().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(generationJobService.createJob(request.definitionIds()));
    }

    @GetMapping("/jobs/{id}")
    public ResponseEntity<GenerationJobResponse> getJob(@PathVariable Long id) {
        return ResponseEntity.ok(generationJobService.getJob(id));
    }

    @GetMapping(value = "/jobs/{id}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter jobEvents(@PathVariable Long id) {
        SseEmitter emitter = new SseEmitter(30_000L);
        try {
            GenerationJobResponse job = generationJobService.getJob(id);
            emitter.send(SseEmitter.event().name("status").data(job));
            if (job.status() == JobStatus.COMPLETED || job.status() == JobStatus.FAILED) {
                emitter.complete();
            }
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
        return emitter;
    }

    @GetMapping("/jobs/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long id) {
        byte[] zip = generationJobService.downloadJobZip(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"generated-project-" + id + ".zip\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(zip);
    }
}
