package com.mahub.core.service;

import com.mahub.core.domain.GeneratedArtifact;
import com.mahub.core.domain.GenerationJob;
import com.mahub.core.domain.InterfaceDefinition;
import com.mahub.core.domain.enums.DefinitionStatus;
import com.mahub.core.domain.enums.JobStatus;
import com.mahub.core.dto.GenerationJobResponse;
import com.mahub.core.repository.GeneratedArtifactRepository;
import com.mahub.core.repository.GenerationJobRepository;
import com.mahub.core.repository.InterfaceDefinitionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.zip.ZipInputStream;
import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenerationJobServiceTest {

    @Mock
    private GenerationJobRepository jobRepository;
    @Mock
    private GeneratedArtifactRepository artifactRepository;
    @Mock
    private InterfaceDefinitionRepository definitionRepository;

    @InjectMocks
    private GenerationJobService generationJobService;

    private InterfaceDefinition sampleDefinition() {
        return InterfaceDefinition.builder()
                .id(1L)
                .serviceName("user-service")
                .fileName("user-service.md")
                .content("# Service: user-service")
                .status(DefinitionStatus.VALID)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private GenerationJob sampleJob(JobStatus status) {
        return GenerationJob.builder()
                .id(1L)
                .definitions(new ArrayList<>(List.of(sampleDefinition())))
                .status(status)
                .createdAt(LocalDateTime.now())
                .progressPercent(0)
                .build();
    }

    @Test
    void createJob_withValidDefinitionIds_returnsJobResponse() {
        when(definitionRepository.findAllById(List.of(1L)))
                .thenReturn(List.of(sampleDefinition()));
        when(jobRepository.save(any())).thenAnswer(inv -> {
            GenerationJob job = inv.getArgument(0);
            job.setId(1L);
            return job;
        });

        GenerationJobResponse response = generationJobService.createJob(List.of(1L));

        assertThat(response.status()).isEqualTo(JobStatus.PENDING);
        assertThat(response.progressPercent()).isEqualTo(0);
        assertThat(response.definitionIds()).contains(1L);
    }

    @Test
    void createJob_nullDefinitionIds_throwsNullPointerException() {
        assertThatThrownBy(() -> generationJobService.createJob(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void createJob_emptyDefinitionIds_createsJobWithNoDefinitions() {
        when(definitionRepository.findAllById(List.of())).thenReturn(List.of());
        when(jobRepository.save(any())).thenAnswer(inv -> {
            GenerationJob job = inv.getArgument(0);
            job.setId(2L);
            return job;
        });

        GenerationJobResponse response = generationJobService.createJob(List.of());

        assertThat(response.status()).isEqualTo(JobStatus.PENDING);
        assertThat(response.definitionIds()).isEmpty();
    }

    @Test
    void getJob_existingId_returnsJobResponse() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(sampleJob(JobStatus.PENDING)));

        GenerationJobResponse response = generationJobService.getJob(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.status()).isEqualTo(JobStatus.PENDING);
    }

    @Test
    void getJob_missingId_throwsNoSuchElementException() {
        when(jobRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> generationJobService.getJob(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("99");
    }

    @Test
    void downloadJobZip_noArtifacts_returnsValidEmptyZip() throws Exception {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(sampleJob(JobStatus.COMPLETED)));
        when(artifactRepository.findByJobId(1L)).thenReturn(List.of());

        byte[] zip = generationJobService.downloadJobZip(1L);

        assertThat(zip).isNotNull();
        ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zip));
        assertThat(zis.getNextEntry()).isNull();
    }

    @Test
    void downloadJobZip_withArtifacts_returnsZipContainingFiles() throws Exception {
        GenerationJob job = sampleJob(JobStatus.COMPLETED);
        GeneratedArtifact artifact = GeneratedArtifact.builder()
                .id(1L)
                .job(job)
                .filePath("user-service/pom.xml")
                .content("<project/>")
                .artifactType("XML")
                .createdAt(LocalDateTime.now())
                .build();

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(artifactRepository.findByJobId(1L)).thenReturn(List.of(artifact));

        byte[] zip = generationJobService.downloadJobZip(1L);

        ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zip));
        assertThat(zis.getNextEntry().getName()).isEqualTo("user-service/pom.xml");
    }

    @Test
    void downloadJobZip_nullId_throwsNullPointerException() {
        assertThatThrownBy(() -> generationJobService.downloadJobZip(null))
                .isInstanceOf(NullPointerException.class);
    }
}
