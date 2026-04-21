package com.mahub.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahub.core.domain.InterfaceDefinition;
import com.mahub.core.domain.enums.DefinitionStatus;
import com.mahub.core.dto.DefinitionUploadResponse;
import com.mahub.core.repository.InterfaceDefinitionRepository;
import com.mahub.parser.MarkdownServiceParser;
import com.mahub.parser.ServiceSpecValidator;
import com.mahub.parser.ValidationError;
import com.mahub.parser.model.ServiceSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DefinitionService {

    private final InterfaceDefinitionRepository repository;
    private final MarkdownServiceParser parser;
    private final ServiceSpecValidator validator;
    private final ObjectMapper objectMapper;

    @Transactional
    public DefinitionUploadResponse upload(String fileName, String markdownContent) {
        Objects.requireNonNull(fileName, "fileName");
        Objects.requireNonNull(markdownContent, "markdownContent");

        ServiceSpec spec = parser.parse(markdownContent);
        List<ValidationError> errors = validator.validate(spec);
        DefinitionStatus status = errors.isEmpty() ? DefinitionStatus.VALID : DefinitionStatus.INVALID;

        String parsedSpecJson = toJson(spec);
        String validationErrorsJson = errors.isEmpty() ? null : toJson(errors);

        String serviceName = spec.name().isBlank() ? fileName : spec.name();

        InterfaceDefinition definition = InterfaceDefinition.builder()
                .serviceName(serviceName)
                .fileName(fileName)
                .content(markdownContent)
                .parsedSpecJson(parsedSpecJson)
                .validationErrors(validationErrorsJson)
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();

        definition = repository.save(definition);
        return DefinitionUploadResponse.from(definition, spec, errors);
    }

    @Transactional(readOnly = true)
    public InterfaceDefinition findById(Long id) {
        Objects.requireNonNull(id, "id");
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Definition not found: " + id));
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON serialization failed", e);
        }
    }
}
