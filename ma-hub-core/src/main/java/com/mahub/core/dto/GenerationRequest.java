package com.mahub.core.dto;

import java.util.List;

public record GenerationRequest(
        List<Long> definitionIds
) {}
