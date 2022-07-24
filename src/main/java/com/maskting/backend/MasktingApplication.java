package com.maskting.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class MasktingApplication {

	public static void main(String[] args) {
		SpringApplication.run(MasktingApplication.class, args);
	}

}
