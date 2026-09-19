package com.hms.guestexperienceservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class GuestExperienceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GuestExperienceServiceApplication.class, args);
    }
}
