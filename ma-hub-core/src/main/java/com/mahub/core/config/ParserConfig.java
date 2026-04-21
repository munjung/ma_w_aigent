package com.mahub.core.config;

import com.mahub.parser.MarkdownServiceParser;
import com.mahub.parser.ServiceSpecValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ParserConfig {

    @Bean
    public MarkdownServiceParser markdownServiceParser() {
        return new MarkdownServiceParser();
    }

    @Bean
    public ServiceSpecValidator serviceSpecValidator() {
        return new ServiceSpecValidator();
    }
}
