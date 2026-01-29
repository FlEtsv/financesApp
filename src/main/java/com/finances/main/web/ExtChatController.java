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
        @RequestHeader(name = "X-API-KEY", required = false) String apiKey,
        @RequestBody AiChatRequest request
    ) {
        if (apiKey == null || !apiKey.equals(aiProperties.getExt().getApiKey())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "API key inválida.");
        }
        return aiChatService.generateReply(request);
    }
}
