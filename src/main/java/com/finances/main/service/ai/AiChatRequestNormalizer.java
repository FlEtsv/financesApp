package com.finances.main.service.ai;

import org.springframework.stereotype.Component;

/**
 * Normaliza los datos entrantes para evitar valores vacíos en el chat.
 */
@Component
public class AiChatRequestNormalizer {
    private static final String DEFAULT_MESSAGE = "Consulta no especificada";

    /**
     * Limpia y homogeniza el mensaje del usuario.
     */
    public String normalizeMessage(String message) {
        if (message == null || message.isBlank()) {
            return DEFAULT_MESSAGE;
        }
        return message.trim();
    }
}
