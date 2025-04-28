package com.blr19c.blr.free.ai.service.deepseek.impl;

import com.blr19c.blr.free.ai.api.dto.ChatCompletionResponse;
import com.blr19c.blr.free.ai.api.vo.ChatAiMessage;
import com.blr19c.blr.free.ai.service.deepseek.DeepSeekChatAiService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * deepseek-r1 文本模型AI
 */
@Service("deepseek-r1")
public class DeepSeekR1ChatAiService extends DeepSeekChatAiService {

    @Override
    public Mono<ChatCompletionResponse> completions(ChatAiMessage chatAiMessage) {
        return completionsDeepSeek(chatAiMessage, "deepseek-r1", true);
    }
}
