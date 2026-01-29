package com.finances.main.service.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.finances.main.web.dto.AiDtos.AiChatRequest;
import com.finances.main.web.dto.AiDtos.AiChatResponse;
import com.finances.main.web.dto.AiDtos.AiContextResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AiChatServiceTest {

    @Test
    void generateReplyUsesContextAndCreatesSessionId() {
        AiProperties properties = new AiProperties();
        properties.setSystemPrompt("Prompt inicial");
        AiPromptBuilder promptBuilder = new AiPromptBuilder(properties);
        AiChatService chatService = new AiChatService(promptBuilder);

        AiContextResponse context = new AiContextResponse(
            "Cuenta principal",
            LocalDate.now().minusDays(7),
            LocalDate.now(),
            null,
            BigDecimal.TEN,
            Map.of("Marketing", BigDecimal.ONE),
            List.of(),
            List.of()
        );

        AiChatRequest request = new AiChatRequest("", "¿Cuál es el balance?", context);
        AiChatResponse response = chatService.generateReply(request);

        assertThat(response.sessionId()).isNotBlank();
        assertThat(response.reply()).contains("Cuenta principal");
    }
}
