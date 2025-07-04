package com.sundrymind.springaitutorial.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.sundrymind.springaitutorial"})
public class SpringAIdemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringAIdemoApplication.class, args);
	}

}
