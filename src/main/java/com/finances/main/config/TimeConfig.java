package com.finances.main.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración centralizada para el manejo de tiempo en servicios.
 */
@Configuration
public class TimeConfig {
    /**
     * Expone un Clock del sistema para facilitar pruebas con inyección.
     */
    @Bean
    public Clock systemClock() {
        return Clock.systemDefaultZone();
    }
}
