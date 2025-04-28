package com.blr19c.blr.free.ai.api.dto;

public record ChatMessage(
        String role,
        Object content,
        String name
) {
}