package com.mahub.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahub.core.domain.enums.JobStatus;
import com.mahub.core.dto.GenerationJobResponse;
import com.mahub.core.dto.GenerationRequest;
import com.mahub.core.service.GenerationJobService;
import com.mahub.web.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({JobController.class, GlobalExceptionHandler.class})
@Import(SecurityConfig.class)
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GenerationJobService generationJobService;

    private GenerationJobResponse pendingJob() {
        return new GenerationJobResponse(
                1L, JobStatus.PENDING, 0, List.of(1L, 2L),
                LocalDateTime.now(), null, null, null
        );
    }

    private GenerationJobResponse completedJob() {
        LocalDateTime now = LocalDateTime.now();
        return new GenerationJobResponse(
                1L, JobStatus.COMPLETED, 100, List.of(1L),
                now, now.minusMinutes(1), now, null
        );
    }

    @Test
    void createJob_validRequest_returns200WithPendingStatus() throws Exception {
        when(generationJobService.createJob(List.of(1L, 2L))).thenReturn(pendingJob());

        GenerationRequest request = new GenerationRequest(List.of(1L, 2L));

        mockMvc.perform(post("/api/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.progressPercent").value(0));
    }

    @Test
    void createJob_returnsJobId() throws Exception {
        when(generationJobService.createJob(any())).thenReturn(pendingJob());

        GenerationRequest request = new GenerationRequest(List.of(1L));

        mockMvc.perform(post("/api/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createJob_emptyDefinitionIds_returns400() throws Exception {
        GenerationRequest request = new GenerationRequest(List.of());

        mockMvc.perform(post("/api/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getJob_existingId_returnsJobResponse() throws Exception {
        when(generationJobService.getJob(1L)).thenReturn(pendingJob());

        mockMvc.perform(get("/api/jobs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getJob_completedJob_returnsCompletedStatus() throws Exception {
        when(generationJobService.getJob(1L)).thenReturn(completedJob());

        mockMvc.perform(get("/api/jobs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.progressPercent").value(100));
    }

    @Test
    void getJob_notExistingId_returns404() throws Exception {
        when(generationJobService.getJob(99L))
                .thenThrow(new NoSuchElementException("Job not found: 99"));

        mockMvc.perform(get("/api/jobs/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void download_existingJob_returnsZipFile() throws Exception {
        byte[] zipBytes = new byte[]{80, 75, 5, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        when(generationJobService.downloadJobZip(1L)).thenReturn(zipBytes);

        mockMvc.perform(get("/api/jobs/1/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"generated-project-1.zip\""))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));
    }
}
