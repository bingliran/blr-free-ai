package com.blr19c.blr.free.ai.api;

import com.blr19c.blr.free.ai.api.dto.ChatCompletionRequest;
import com.blr19c.blr.free.ai.api.dto.ChatCompletionResponse;
import com.blr19c.blr.free.ai.api.dto.ChatMessage;
import com.blr19c.blr.free.ai.api.vo.ChatAiMessage;
import com.blr19c.blr.free.ai.config.exception.AiException;
import com.blr19c.blr.free.ai.service.ChatAiService;
import com.blr19c.blr.free.ai.utils.application.ApplicationUtils;
import com.blr19c.blr.free.ai.utils.client.WebClientUtils;
import com.blr19c.blr.free.ai.utils.json.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;

/**
 * 文本生成模型
 */
@RestController
@RequestMapping("/v1/chat")
@AllArgsConstructor
@Slf4j
public class ChatApi {

    private final Map<String, ChatAiService> chatAiServiceMap;

    @PostMapping("/completions")
    public Mono<ChatCompletionResponse> completions(@RequestBody ChatCompletionRequest chatCompletionRequest) {
        log.info("文本生成模型-收到请求:{}", chatCompletionRequest);
        return Mono.justOrEmpty(chatAiServiceMap.get(chatCompletionRequest.model()))
                .switchIfEmpty(Mono.error(new AiException("未找到模型:" + chatCompletionRequest.model())))
                .flatMap(chatAiService -> completions(chatAiService, chatCompletionRequest))
                .timeout(Duration.ofMinutes(10))
                .switchIfEmpty(Mono.error(new AiException("模型无返回结果")))
                .onErrorResume(throwable -> Mono.error(new AiException("模型处理异常:" + throwable.getMessage())))
                .doOnNext(chatCompletionResponse -> log.info("文本生成模型-返回结果:{}", chatCompletionResponse));
    }

    private Mono<ChatCompletionResponse> completions(ChatAiService chatAiService, ChatCompletionRequest chatCompletionRequest) {
        return toChatMessage(chatCompletionRequest).flatMap(chatAiMessage -> chatAiService.completions(chatAiMessage)
                .doFinally(signalType -> cleanImagePath(chatAiMessage.imageList()))
        );
    }

    private Mono<ChatAiMessage> toChatMessage(ChatCompletionRequest chatCompletionRequest) {
        StringJoiner joiner = new StringJoiner("\n");
        joiner.add("1.如果需要使用图片,图片在附件中");
        joiner.add("2.下面是一段API格式的历史对话,你需要解析并提供最新的返回结果(不需要带有这个API的格式)");
        joiner.add("3.禁止携带除了system预设之外的任何格式");
        List<Mono<Path>> imagePathList = new ArrayList<>();
        for (ChatMessage message : chatCompletionRequest.messages()) {
            if (message.content() instanceof String content) {
                joiner.add(message.role() + "-" + message.name() + ": " + content);
                continue;
            }
            ArrayNode content = (ArrayNode) JsonUtils.readJsonNode(JsonUtils.toJsonString(message.content()));
            for (JsonNode node : content) {
                if ("text".equals(node.path("type").asText())) {
                    joiner.add(message.role() + "-" + message.name() + ": " + node.path("text").asText());
                }
                if ("image_url".equals(node.path("type").asText())) {
                    imagePathList.add(imageToPath(node.path("image_url").path("url").asText()));
                }
            }
        }
        return Flux.merge(imagePathList)
                .collectList()
                .map(imagePaths -> new ChatAiMessage(joiner.toString(), imagePaths));
    }

    private void cleanImagePath(List<Path> paths) {
        paths.forEach(path -> {
            try {
                Files.delete(path);
            } catch (IOException ignored) {

            }
        });
    }

    private Mono<Path> imageToPath(String image) {
        return Mono.defer(() -> {
            if (image.startsWith("http:") || image.startsWith("https:")) {
                return httpImageToPath(URI.create(image));
            }
            if (image.startsWith("data:image/png;base64,") || image.startsWith("data:image/jpeg;base64,") || image.startsWith("data:image/jpg;base64,")) {
                return base64ImageToPath(image);
            }
            return Mono.error(new AiException("无法处理的图片"));
        });
    }

    private Mono<Path> httpImageToPath(URI image) {
        return Mono.defer(() -> {
            Path imagePath = ApplicationUtils.getLocalDir().resolve("image-temp").resolve(UUID.randomUUID() + ".png");
            return WebClientUtils.longTimeoutWebClient()
                    .get()
                    .uri(image)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .map(ByteArrayInputStream::new)
                    .flatMap(inputStream -> Mono.fromRunnable(() -> {
                        try {
                            FileUtils.copyInputStreamToFile(inputStream, imagePath.toFile());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })).thenReturn(imagePath);
        });
    }

    private Mono<Path> base64ImageToPath(String image) {
        return Mono.fromCallable(() -> {
            Path imagePath = ApplicationUtils.getLocalDir().resolve("image-temp").resolve(UUID.randomUUID() + ".png");
            String imageBase64 = image.substring(image.indexOf(",") + 1);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(imageBase64));
            try {
                FileUtils.copyInputStreamToFile(inputStream, imagePath.toFile());
                return imagePath;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
