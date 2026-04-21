package com.mahub.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.mahub")
public class MaHubApplication {
    public static void main(String[] args) {
        SpringApplication.run(MaHubApplication.class, args);
    }
}
