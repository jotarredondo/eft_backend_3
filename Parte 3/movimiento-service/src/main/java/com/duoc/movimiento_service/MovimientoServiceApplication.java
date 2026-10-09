package com.duoc.movimiento_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class MovimientoServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MovimientoServiceApplication.class, args);
	}

}
