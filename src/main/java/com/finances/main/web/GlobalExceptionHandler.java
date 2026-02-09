package com.finances.main.web;

import com.finances.main.web.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {

        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList();

        ErrorResponse body = new ErrorResponse(
                Instant.now(),
                MDC.get("correlationId"),
                req.getRequestURI(),
                "VALIDATION_ERROR",
                "Datos inválidos",
                details
        );

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex, HttpServletRequest req) {

        String correlationId = MDC.get("correlationId");

        ErrorResponse body = new ErrorResponse(
                Instant.now(),
                correlationId,
                req.getRequestURI(),
                ex.getStatusCode().toString(),
                ex.getReason() != null ? ex.getReason() : "Error de solicitud",
                List.of()
        );

        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {

        String correlationId = MDC.get("correlationId");
        log.error("Unhandled error. correlationId={} path={}", correlationId, req.getRequestURI(), ex);

        ErrorResponse body = new ErrorResponse(
                Instant.now(),
                correlationId,
                req.getRequestURI(),
                "INTERNAL_ERROR",
                "Error interno",
                List.of()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}

