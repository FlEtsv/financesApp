package com.finances.main.web;

import com.finances.main.service.ai.AiChatService;
import com.finances.main.service.ai.AiProperties;
import com.finances.main.web.dto.AiDtos.AiChatRequest;
import com.finances.main.web.dto.AiDtos.AiChatResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Endpoint externo para consumo de chat IA sin cookies ni CSRF.
 */
@RestController
@RequestMapping("/api/ext")
public class ExtChatController {
    private final AiChatService aiChatService;
    private final AiProperties aiProperties;

    public ExtChatController(AiChatService aiChatService, AiProperties aiProperties) {
        this.aiChatService = aiChatService;
        this.aiProperties = aiProperties;
    }

    /**
     * Procesa solicitudes de chat autenticadas con API key.
     */
    @PostMapping("/chat")
    @ResponseStatus(HttpStatus.OK)
    public AiChatResponse chat(

        @RequestHeader(name = "x-api-key", required = false) String apiKey,
        @RequestBody(required = false) AiChatRequest request
    ) {
        if (aiProperties.getExt() == null || aiProperties.getExt().getApiKey() == null
            || aiProperties.getExt().getApiKey().isBlank()) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Configuración de IA externa incompleta (ext.apiKey)."
            );
        }

        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Falta x-api-key.");
        }

        String normalizedApiKey = apiKey.trim();
        String expectedApiKey = aiProperties.getExt().getApiKey().trim();

        if (!normalizedApiKey.equals(expectedApiKey)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "API key inválida.");
        }

        if (request == null || request.message() == null || request.message().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El campo 'message' es obligatorio.");
        }

        return aiChatService.generateReply(request);
    }
}
