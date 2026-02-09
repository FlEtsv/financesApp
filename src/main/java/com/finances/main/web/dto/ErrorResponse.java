package com.finances.main.web.dto;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        Instant timestamp,
        String correlationId,
        String path,
        String errorCode,
        String message,
        List<String> details
) {}

