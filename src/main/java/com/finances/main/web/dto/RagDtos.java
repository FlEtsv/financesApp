package com.finances.main.web.dto;

public final class RagDtos {
    private RagDtos() {}

    public record RagDocumentRequest(String title, String content) {}
    public record RagDocumentResponse(String status, String id) {}
}
