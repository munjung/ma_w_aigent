package com.mahub.core.repository;

import com.mahub.core.domain.GenerationJob;
import com.mahub.core.domain.enums.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GenerationJobRepository extends JpaRepository<GenerationJob, Long> {
    List<GenerationJob> findByStatus(JobStatus status);
    List<GenerationJob> findByStatusOrderByCreatedAtDesc(JobStatus status);
}
