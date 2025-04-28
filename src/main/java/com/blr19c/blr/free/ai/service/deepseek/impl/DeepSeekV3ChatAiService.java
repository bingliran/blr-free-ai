package com.blr19c.blr.free.ai.service.deepseek.impl;

import com.blr19c.blr.free.ai.api.dto.ChatCompletionResponse;
import com.blr19c.blr.free.ai.api.vo.ChatAiMessage;
import com.blr19c.blr.free.ai.service.deepseek.DeepSeekChatAiService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * deepseek-v3 文本模型AI
 */
@Service("deepseek-v3")
public class DeepSeekV3ChatAiService extends DeepSeekChatAiService {

    @Override
    public Mono<ChatCompletionResponse> completions(ChatAiMessage chatAiMessage) {
        return completionsDeepSeek(chatAiMessage, "deepseek-v3", false);
    }
}
