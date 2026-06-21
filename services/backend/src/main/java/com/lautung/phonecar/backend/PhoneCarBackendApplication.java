package com.lautung.phonecar.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PhoneCarBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(PhoneCarBackendApplication.class, args);
    }
}
