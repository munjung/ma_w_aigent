package com.mahub.core.dto;

import com.mahub.core.domain.InterfaceDefinition;
import com.mahub.parser.ValidationError;
import com.mahub.parser.model.ServiceSpec;

import java.time.LocalDateTime;
import java.util.List;

public record DefinitionUploadResponse(
        Long id,
        String serviceName,
        String fileName,
        String status,
        ServiceSpec parsedSpec,
        List<ValidationError> validationErrors,
        LocalDateTime createdAt
) {
    public static DefinitionUploadResponse from(
            InterfaceDefinition def, ServiceSpec spec, List<ValidationError> errors) {
        return new DefinitionUploadResponse(
                def.getId(),
                def.getServiceName(),
                def.getFileName(),
                def.getStatus().name(),
                spec,
                errors,
                def.getCreatedAt()
        );
    }
}
