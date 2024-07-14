package com.bmservice.core;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableEncryptableProperties
public class BmserviceCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(BmserviceCoreApplication.class, args);
    }


}
