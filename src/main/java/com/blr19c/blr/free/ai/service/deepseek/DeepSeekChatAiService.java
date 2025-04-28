package com.blr19c.blr.free.ai.service.deepseek;

import com.blr19c.blr.free.ai.api.dto.ChatCompletionResponse;
import com.blr19c.blr.free.ai.api.vo.ChatAiMessage;
import com.blr19c.blr.free.ai.service.ChatAiService;
import com.blr19c.blr.free.ai.utils.json.JsonUtils;
import com.blr19c.blr.free.ai.utils.playwright.PlaywrightUtils;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * deepseek文本模型AI
 */
@Slf4j
public abstract class DeepSeekChatAiService extends ChatAiService {

    /**
     * deepseek生成
     *
     * @param chatAiMessage 输入信息
     * @param model         模型
     * @param deepThink     深度思考
     */
    protected Mono<ChatCompletionResponse> completionsDeepSeek(ChatAiMessage chatAiMessage, String model, boolean deepThink) {
        return Mono.create(sink -> PlaywrightUtils.acceptPage((info, page) -> {
            CompletableFuture<ChatCompletionResponse> future = pageMonitor(page, model);
            pageOption(page, chatAiMessage, model, deepThink);
            try {
                sink.success(future.get(10, TimeUnit.MINUTES));
            } catch (Exception e) {
                sink.error(e);
            }
        }));
    }

    /**
     * 页面监听
     */
    private CompletableFuture<ChatCompletionResponse> pageMonitor(Page page, String model) {
        CompletableFuture<ChatCompletionResponse> responseFuture = new CompletableFuture<>();
        page.route(Pattern.compile("https://yuanbao.tencent.com/api/chat/.+"), route -> {
            try {
                APIResponse fetch = route.fetch();
                fetch.headers().put("Content-Type", "text/event-stream; charset=utf-8");
                responseFuture.complete(parseChatEventStream(fetch.text(), model));
            } catch (Exception e) {
                responseFuture.completeExceptionally(e);
            }
        });
        return responseFuture;
    }


    /**
     * 页面元素操作
     */
    private void pageOption(Page page, ChatAiMessage chatAiMessage, String model, boolean deepThink) {
        page.navigate("https://yuanbao.tencent.com");
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.click(".yb-common-nav__icon-btn");
        page.click(".ql-editor");
        page.querySelector(".ql-editor").fill(chatAiMessage.chatMessage());
        page.querySelector("button[dt-button-id='model_switch']").click();
        page.querySelectorAll("div[class='drop-down-item__name']")
                .stream()
                .filter(e -> model.toLowerCase().contains(e.innerText().toLowerCase()))
                .findFirst()
                .ifPresent(ElementHandle::click);
        ElementHandle deepThinkEl = page.querySelector("button[dt-button-id='deep_think']");
        String deepClass = deepThinkEl.getAttribute("class");
        if (deepClass.contains("checked") != deepThink) {
            deepThinkEl.click();
        }
        pageFileOption(page, chatAiMessage.imageList());
        page.querySelector(".icon-send").click();
    }

    /**
     * 页面操作上传图片
     */
    private void pageFileOption(Page page, List<Path> images) {
        if (CollectionUtils.isEmpty(images)) return;
        Locator locator = page.locator("input[type='file'][accept='capture=filesystem,.jpg,.jpeg,.png,.webp,.bmp,.gif']").nth(0);
        images.forEach(locator::setInputFiles);
        page.waitForTimeout(500);
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(1000);
    }


    /**
     * 解析EventStream到ChatCompletionResponse
     */
    private ChatCompletionResponse parseChatEventStream(String source, String model) {
        log.info("开始解析EventStream");
        String[] splitLine = source.split("\n");
        String chatMessage = Arrays.stream(splitLine)
                .filter(line -> line.startsWith("data:") && line.contains("\"type\":\"text\""))
                .map(line -> line.trim().substring(6))
                .map(JsonUtils::readJsonNode)
                .map(node -> node.path("msg").asText())
                .collect(Collectors.joining())
                .trim();
        String messageId = Arrays.stream(splitLine).filter(line -> line.startsWith("data:") && line.contains("\"type\":\"meta\""))
                .map(line -> line.trim().substring(6))
                .map(JsonUtils::readJsonNode)
                .map(node -> node.path("messageId").asText())
                .findFirst()
                .orElseGet(() -> UUID.randomUUID().toString());
        log.info("解析EventStream完成,{}", messageId);
        return parseChatCompletionResponse(messageId, chatMessage, model);
    }
}
