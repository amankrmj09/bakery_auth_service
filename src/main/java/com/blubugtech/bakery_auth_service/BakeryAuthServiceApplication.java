package com.blubugtech.bakery_auth_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableDiscoveryClient

@EnableCaching
public class BakeryAuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BakeryAuthServiceApplication.class, args);
	}

}
