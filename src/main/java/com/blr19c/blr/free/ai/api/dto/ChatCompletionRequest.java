package com.blr19c.blr.free.ai.api.dto;

import java.util.List;

public record ChatCompletionRequest(
        String model,
        List<ChatMessage> messages
) {
}
