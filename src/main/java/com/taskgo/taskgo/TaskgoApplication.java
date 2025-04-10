package com.taskgo.taskgo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.taskgo.taskgo.model") // Escanea entidades en este paquete
@EnableJpaRepositories("com.taskgo.taskgo.repository") // Escanea repositorios en este paquete
public class TaskgoApplication {
	public static void main(String[] args) {
		SpringApplication.run(TaskgoApplication.class, args);
	}
}