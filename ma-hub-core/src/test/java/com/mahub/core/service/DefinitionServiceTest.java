package com.mahub.core.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahub.core.domain.InterfaceDefinition;
import com.mahub.core.domain.enums.DefinitionStatus;
import com.mahub.core.dto.DefinitionUploadResponse;
import com.mahub.core.repository.InterfaceDefinitionRepository;
import com.mahub.parser.MarkdownServiceParser;
import com.mahub.parser.ServiceSpecValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefinitionServiceTest {

    @Mock
    private InterfaceDefinitionRepository repository;

    private DefinitionService definitionService;

    private static final String VALID_MD = """
            # Service: test-service

            ## Description
            테스트 서비스

            ## Domain Models

            ### User
            - id: Long [PK, auto]
            - name: String [required]

            ## APIs

            ### GET /users/{id}
            - Summary: 사용자 조회
            - Auth: jwt
            - Response: UserResponse { id: Long, name: String }
            - Errors: 404

            ## Communication
            - Outbound: rest
            - DB: postgresql

            ## Config
            - port: 8081
            - context-path: /test-service
            """;

    @BeforeEach
    void setUp() {
        definitionService = new DefinitionService(
                repository,
                new MarkdownServiceParser(),
                new ServiceSpecValidator(),
                new ObjectMapper()
        );
    }

    @Test
    void upload_validMarkdown_returnsValidStatus() {
        when(repository.save(any())).thenAnswer(inv -> {
            InterfaceDefinition d = inv.getArgument(0);
            d.setId(1L);
            return d;
        });

        DefinitionUploadResponse response = definitionService.upload("test-service.md", VALID_MD);

        assertThat(response.status()).isEqualTo("VALID");
        assertThat(response.serviceName()).isEqualTo("test-service");
        assertThat(response.validationErrors()).isEmpty();
    }

    @Test
    void upload_validMarkdown_parsesServiceSpec() {
        when(repository.save(any())).thenAnswer(inv -> {
            InterfaceDefinition d = inv.getArgument(0);
            d.setId(1L);
            return d;
        });

        DefinitionUploadResponse response = definitionService.upload("test-service.md", VALID_MD);

        assertThat(response.parsedSpec().name()).isEqualTo("test-service");
        assertThat(response.parsedSpec().apiEndpoints()).hasSize(1);
        assertThat(response.parsedSpec().port()).isEqualTo(8081);
    }

    @Test
    void upload_invalidMarkdown_returnsInvalidStatus() {
        when(repository.save(any())).thenAnswer(inv -> {
            InterfaceDefinition d = inv.getArgument(0);
            d.setId(1L);
            return d;
        });

        DefinitionUploadResponse response = definitionService.upload("empty.md", "");

        assertThat(response.status()).isEqualTo("INVALID");
        assertThat(response.validationErrors()).isNotEmpty();
    }

    @Test
    void upload_savesDefinitionToRepository() {
        when(repository.save(any())).thenAnswer(inv -> {
            InterfaceDefinition d = inv.getArgument(0);
            d.setId(1L);
            return d;
        });

        definitionService.upload("test-service.md", VALID_MD);

        verify(repository, times(1)).save(any(InterfaceDefinition.class));
    }

    @Test
    void upload_nullFileName_throwsNullPointerException() {
        assertThatThrownBy(() -> definitionService.upload(null, VALID_MD))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void upload_nullContent_throwsNullPointerException() {
        assertThatThrownBy(() -> definitionService.upload("test.md", null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void findById_existingId_returnsDefinition() {
        InterfaceDefinition def = InterfaceDefinition.builder()
                .id(1L)
                .serviceName("test-service")
                .fileName("test.md")
                .content(VALID_MD)
                .status(DefinitionStatus.VALID)
                .createdAt(LocalDateTime.now())
                .build();
        when(repository.findById(1L)).thenReturn(Optional.of(def));

        InterfaceDefinition found = definitionService.findById(1L);

        assertThat(found.getId()).isEqualTo(1L);
        assertThat(found.getServiceName()).isEqualTo("test-service");
    }

    @Test
    void findById_missingId_throwsNoSuchElementException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> definitionService.findById(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("99");
    }
}
