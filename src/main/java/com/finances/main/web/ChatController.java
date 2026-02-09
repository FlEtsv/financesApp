package com.finances.main.web;

import com.finances.main.service.ai.ExtChatClient;
import com.finances.main.service.ai.ExternalAiUnavailableException;
import com.finances.main.web.dto.AiDtos.AiChatRequest;
import com.finances.main.web.dto.AiDtos.AiChatResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ExtChatClient extChatClient;

    public ChatController(ExtChatClient extChatClient) {
        this.extChatClient = extChatClient;
    }

    @PostMapping("/chat")
    @ResponseStatus(HttpStatus.OK)
    public AiChatResponse chat(@RequestBody(required = false) AiChatRequest request) {

        if (request == null || request.message() == null || request.message().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El campo 'message' es obligatorio.");
        }

        try {
            return extChatClient.sendChat(request);
        } catch (ExternalAiUnavailableException ex) {
            throw new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "El proveedor de IA no respondió. Revisa la configuración.",
                ex
            );
        }
    }
}
