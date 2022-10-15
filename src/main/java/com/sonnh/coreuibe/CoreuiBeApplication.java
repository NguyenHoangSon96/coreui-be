package com.sonnh.coreuibe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class CoreuiBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoreuiBeApplication.class, args);
	}

}
