package com.bmservice.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableEncryptableProperties
public class BmserviceCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(BmserviceCoreApplication.class, args);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}
