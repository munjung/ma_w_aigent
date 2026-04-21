package com.mahub.web.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = "com.mahub.core.domain")
@EnableJpaRepositories(basePackages = "com.mahub.core.repository")
public class JpaConfig {
}
