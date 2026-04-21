package com.mahub.core.service;

import com.mahub.core.domain.GeneratedArtifact;
import com.mahub.core.domain.GenerationJob;
import com.mahub.core.domain.InterfaceDefinition;
import com.mahub.core.domain.enums.JobStatus;
import com.mahub.core.dto.GenerationJobResponse;
import com.mahub.core.repository.GeneratedArtifactRepository;
import com.mahub.core.repository.GenerationJobRepository;
import com.mahub.core.repository.InterfaceDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class GenerationJobService {

    private final GenerationJobRepository jobRepository;
    private final GeneratedArtifactRepository artifactRepository;
    private final InterfaceDefinitionRepository definitionRepository;

    @Transactional
    public GenerationJobResponse createJob(List<Long> definitionIds) {
        Objects.requireNonNull(definitionIds, "definitionIds");

        List<InterfaceDefinition> definitions = definitionRepository.findAllById(definitionIds);

        GenerationJob job = GenerationJob.builder()
                .definitions(new ArrayList<>(definitions))
                .status(JobStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .progressPercent(0)
                .build();

        return GenerationJobResponse.from(jobRepository.save(job));
    }

    @Transactional(readOnly = true)
    public GenerationJobResponse getJob(Long id) {
        Objects.requireNonNull(id, "id");
        GenerationJob job = jobRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Job not found: " + id));
        return GenerationJobResponse.from(job);
    }

    @Transactional(readOnly = true)
    public byte[] downloadJobZip(Long id) {
        Objects.requireNonNull(id, "id");
        GenerationJob job = jobRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Job not found: " + id));

        List<GeneratedArtifact> artifacts = artifactRepository.findByJobId(job.getId());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(baos)) {
            for (GeneratedArtifact artifact : artifacts) {
                zip.putNextEntry(new ZipEntry(artifact.getFilePath()));
                if (artifact.getContent() != null) {
                    zip.write(artifact.getContent().getBytes(StandardCharsets.UTF_8));
                }
                zip.closeEntry();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create ZIP", e);
        }

        return baos.toByteArray();
    }
}
