package com.finances.main.service.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.finances.main.web.dto.AiDtos.AiChatRequest;
import com.finances.main.web.dto.AiDtos.AiContextResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AiPromptBuilderTest {

    @Test
    void buildPromptSkipsNullContextValuesAndNormalizesMessage() {
        AiProperties properties = new AiProperties();
        properties.setSystemPrompt("Prompt del sistema");
        AiChatRequestNormalizer normalizer = new AiChatRequestNormalizer();
        AiPromptBuilder promptBuilder = new AiPromptBuilder(properties, normalizer);

        AiContextResponse context = new AiContextResponse(
            "Cuenta demo",
            LocalDate.now().minusDays(5),
            LocalDate.now(),
            null,
            BigDecimal.ZERO,
            Map.of(),
            List.of(),
            List.of()
        );

        AiChatRequest request = new AiChatRequest(null, "   ", context);
        String prompt = promptBuilder.buildPrompt(request);

        assertThat(prompt).contains("Prompt del sistema");
        assertThat(prompt).contains("Mensaje del usuario: Consulta no especificada");
        assertThat(prompt).contains("Cuenta: Cuenta demo");
        assertThat(prompt).contains("Balance: 0");
        assertThat(prompt).doesNotContain("Totales por categoría: {}");
    }
}
