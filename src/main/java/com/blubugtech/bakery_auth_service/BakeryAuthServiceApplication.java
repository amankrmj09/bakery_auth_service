package com.blubugtech.bakery_auth_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient

@EnableCaching
public class BakeryAuthServiceApplication {

    static void main(String[] args) {
        SpringApplication.run(BakeryAuthServiceApplication.class, args);
    }

}
