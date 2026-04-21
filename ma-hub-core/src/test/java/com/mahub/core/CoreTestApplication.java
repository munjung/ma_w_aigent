package com.mahub.core;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.mahub.core.domain")
@EnableJpaRepositories("com.mahub.core.repository")
class CoreTestApplication {
}
