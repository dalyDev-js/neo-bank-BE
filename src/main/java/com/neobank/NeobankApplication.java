package com.neobank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class NeobankApplication {

	public static void main(String[] args) {
		SpringApplication.run(NeobankApplication.class, args);
		System.out.println("Hello World");
	}

}
