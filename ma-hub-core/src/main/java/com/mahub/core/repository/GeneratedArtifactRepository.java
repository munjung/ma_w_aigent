package com.mahub.core.repository;

import com.mahub.core.domain.GeneratedArtifact;
import com.mahub.core.domain.GenerationJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GeneratedArtifactRepository extends JpaRepository<GeneratedArtifact, Long> {
    List<GeneratedArtifact> findByJob(GenerationJob job);
    List<GeneratedArtifact> findByJobId(Long jobId);
}
