package com.mahub.core.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "generated_artifact")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedArtifact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private GenerationJob job;

    @Column(nullable = false)
    private String filePath;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private String artifactType;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
