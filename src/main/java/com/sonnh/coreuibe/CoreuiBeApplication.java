package com.sonnh.coreuibe;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.persistence.Query;

@SpringBootApplication
@EnableJpaRepositories
public class CoreuiBeApplication {

	public static void main(String[] args) {

		SpringApplication.run(CoreuiBeApplication.class, args);
	}

}
