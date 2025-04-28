package com.blr19c.blr.free.ai.api.dto;

import java.util.List;

public record ChatCompletionResponse(
        String id,
        String object,
        long created,
        String model,
        List<Choice> choices
) {
    public record Choice(
            int index,
            ChatMessage message
    ) {
    }
}