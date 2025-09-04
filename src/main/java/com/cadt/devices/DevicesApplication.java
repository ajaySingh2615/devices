package com.cadt.devices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class DevicesApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevicesApplication.class, args);
    }

}
