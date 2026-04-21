package com.mahub.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahub.core.domain.enums.DefinitionStatus;
import com.mahub.core.dto.DefinitionUploadResponse;
import com.mahub.core.service.DefinitionService;
import com.mahub.parser.model.*;
import com.mahub.web.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DefinitionController.class)
@Import(SecurityConfig.class)
class DefinitionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DefinitionService definitionService;

    private DefinitionUploadResponse validResponse() {
        ServiceSpec spec = new ServiceSpec(
                "user-service", "사용자 서비스",
                List.of(), List.of(
                new ApiEndpoint("GET", "/users/{id}", "사용자 조회", "jwt", null,
                        new DtoSpec("UserResponse", List.of()), List.of(404))
        ),
                List.of(), List.of(), List.of(), "rest", "postgresql", 8081, "/user-service"
        );
        return new DefinitionUploadResponse(
                1L, "user-service", "user-service.md",
                DefinitionStatus.VALID.name(), spec, List.of(), LocalDateTime.now()
        );
    }

    @Test
    void upload_validMdFile_returns200WithParsedSpec() throws Exception {
        when(definitionService.upload(anyString(), anyString())).thenReturn(validResponse());

        MockMultipartFile file = new MockMultipartFile(
                "file", "user-service.md", "text/markdown",
                "# Service: user-service".getBytes()
        );

        mockMvc.perform(multipart("/api/definitions").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VALID"))
                .andExpect(jsonPath("$.serviceName").value("user-service"));
    }

    @Test
    void upload_returnsDefinitionId() throws Exception {
        when(definitionService.upload(anyString(), anyString())).thenReturn(validResponse());

        MockMultipartFile file = new MockMultipartFile(
                "file", "user-service.md", "text/markdown",
                "# Service: user-service".getBytes()
        );

        mockMvc.perform(multipart("/api/definitions").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void upload_returnsParsedSpecDetails() throws Exception {
        when(definitionService.upload(anyString(), anyString())).thenReturn(validResponse());

        MockMultipartFile file = new MockMultipartFile(
                "file", "user-service.md", "text/markdown",
                "# Service: user-service".getBytes()
        );

        mockMvc.perform(multipart("/api/definitions").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parsedSpec.name").value("user-service"))
                .andExpect(jsonPath("$.parsedSpec.port").value(8081));
    }

    @Test
    void upload_missingFile_returns400() throws Exception {
        mockMvc.perform(multipart("/api/definitions"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void upload_invalidResponse_returnsInvalidStatus() throws Exception {
        DefinitionUploadResponse invalidResp = new DefinitionUploadResponse(
                2L, "bad-service", "bad.md", DefinitionStatus.INVALID.name(),
                new ServiceSpec("", "", List.of(), List.of(), List.of(), List.of(), List.of(),
                        "rest", "postgresql", 8080, "/"),
                List.of(new com.mahub.parser.ValidationError(
                        com.mahub.parser.ValidationError.Severity.ERROR, "name", "서비스명이 누락되었습니다.")),
                LocalDateTime.now()
        );
        when(definitionService.upload(anyString(), anyString())).thenReturn(invalidResp);

        MockMultipartFile file = new MockMultipartFile(
                "file", "bad.md", "text/markdown", "".getBytes()
        );

        mockMvc.perform(multipart("/api/definitions").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INVALID"))
                .andExpect(jsonPath("$.validationErrors").isArray());
    }

    @Test
    void upload_returnsJsonContentType() throws Exception {
        when(definitionService.upload(anyString(), anyString())).thenReturn(validResponse());

        MockMultipartFile file = new MockMultipartFile(
                "file", "user-service.md", "text/markdown",
                "# Service: user-service".getBytes()
        );

        mockMvc.perform(multipart("/api/definitions").file(file))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"));
    }
}
