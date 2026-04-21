package com.mahub.core.domain;

import com.mahub.core.domain.enums.DefinitionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "interface_definition")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterfaceDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String serviceName;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String parsedSpecJson;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DefinitionStatus status;

    @Column(columnDefinition = "TEXT")
    private String validationErrors;
}
