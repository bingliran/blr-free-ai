package com.blr19c.blr.free.ai.service;

import com.blr19c.blr.free.ai.api.dto.ChatCompletionResponse;
import com.blr19c.blr.free.ai.api.dto.ChatMessage;
import com.blr19c.blr.free.ai.api.vo.ChatAiMessage;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 文本模型AI
 */
public abstract class ChatAiService {

    public abstract Mono<ChatCompletionResponse> completions(ChatAiMessage chatAiMessage);

    protected ChatCompletionResponse parseChatCompletionResponse(String id, String chatMessage, String model) {
        return new ChatCompletionResponse(
                id,
                "chat.completion",
                System.currentTimeMillis(),
                model,
                List.of(new ChatCompletionResponse.Choice(0, new ChatMessage("assistant", chatMessage, null)))
        );
    }
}
