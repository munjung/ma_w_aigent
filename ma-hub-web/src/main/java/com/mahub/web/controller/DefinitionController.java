package com.mahub.web.controller;

import com.mahub.core.dto.DefinitionUploadResponse;
import com.mahub.core.service.DefinitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@RestController
@RequestMapping("/api/definitions")
@RequiredArgsConstructor
public class DefinitionController {

    private final DefinitionService definitionService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DefinitionUploadResponse> upload(
            @RequestParam("file") MultipartFile file) throws IOException {
        String fileName = Objects.requireNonNull(file.getOriginalFilename(), "fileName");
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        return ResponseEntity.ok(definitionService.upload(fileName, content));
    }
}
