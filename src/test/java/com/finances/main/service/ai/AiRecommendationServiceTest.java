package com.finances.main.service.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.finances.main.model.CategoryType;
import com.finances.main.service.AccountService;
import com.finances.main.web.dto.AiDtos.AiChatResponse;
import com.finances.main.web.dto.AiDtos.AiContextResponse;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AiRecommendationServiceTest {
    @Mock
    private AiContextService aiContextService;

    @Mock
    private ExtChatClient extChatClient;

    @Mock
    private AccountService accountService;

    @Test
    void onMovementRecordedGeneratesRecommendationWhenEnabled() {
        AiProperties properties = new AiProperties();
        properties.getRecommendations().setEnabled(true);
        properties.getRecommendations().setLookbackDays(7);
        properties.getRecommendations().setCategoryType(CategoryType.GASTO);

        Instant now = Instant.parse("2024-06-15T10:00:00Z");
        Clock clock = Clock.fixed(now, ZoneOffset.UTC);
        LocalDate expectedEnd = LocalDate.of(2024, 6, 15);
        LocalDate expectedStart = expectedEnd.minusDays(7);

        AiContextResponse context = new AiContextResponse(
            "Cuenta",
            expectedStart,
            expectedEnd,
            CategoryType.GASTO,
            BigDecimal.TEN,
            Map.of("Marketing", BigDecimal.ONE),
            List.of(),
            List.of()
        );
        AiChatResponse chatResponse = new AiChatResponse("session", "Respuesta IA", now);

        when(aiContextService.buildContext("Cuenta", expectedStart, expectedEnd, CategoryType.GASTO))
            .thenReturn(context);
        when(extChatClient.sendChat(any())).thenReturn(chatResponse);

        AiRecommendationService service = new AiRecommendationService(
            aiContextService,
            extChatClient,
            accountService,
            properties,
            clock
        );

        service.onMovementRecorded("Cuenta");

        Optional<AiRecommendationService.AiRecommendationSnapshot> snapshot =
            service.getLatestRecommendation("Cuenta");

        assertThat(snapshot).isPresent();
        assertThat(snapshot.get().recommendation()).isEqualTo("Respuesta IA");
        verify(aiContextService).buildContext("Cuenta", expectedStart, expectedEnd, CategoryType.GASTO);
        verify(extChatClient).sendChat(any());
    }

    @Test
    void onMovementRecordedSkipsWhenDisabled() {
        AiProperties properties = new AiProperties();
        properties.getRecommendations().setEnabled(false);

        AiRecommendationService service = new AiRecommendationService(
            aiContextService,
            extChatClient,
            accountService,
            properties,
            Clock.systemUTC()
        );

        service.onMovementRecorded("Cuenta");

        verify(aiContextService, never()).buildContext(eq("Cuenta"), any(), any(), any());
        verify(extChatClient, never()).sendChat(any());
    }
}
