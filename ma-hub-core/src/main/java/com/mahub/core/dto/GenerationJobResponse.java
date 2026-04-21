package com.mahub.core.dto;

import com.mahub.core.domain.GenerationJob;
import com.mahub.core.domain.InterfaceDefinition;
import com.mahub.core.domain.enums.JobStatus;

import java.time.LocalDateTime;
import java.util.List;

public record GenerationJobResponse(
        Long id,
        JobStatus status,
        int progressPercent,
        List<Long> definitionIds,
        LocalDateTime createdAt,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        String errorMessage
) {
    public static GenerationJobResponse from(GenerationJob job) {
        return new GenerationJobResponse(
                job.getId(),
                job.getStatus(),
                job.getProgressPercent(),
                job.getDefinitions().stream().map(InterfaceDefinition::getId).toList(),
                job.getCreatedAt(),
                job.getStartedAt(),
                job.getCompletedAt(),
                job.getErrorMessage()
        );
    }
}
