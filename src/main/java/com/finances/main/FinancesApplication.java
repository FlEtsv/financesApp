package com.finances.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Punto de entrada de la aplicación de finanzas.
 */
@SpringBootApplication
@EnableScheduling
public class FinancesApplication {
    public static void main(String[] args) {
        SpringApplication.run(FinancesApplication.class, args);
    }
}
