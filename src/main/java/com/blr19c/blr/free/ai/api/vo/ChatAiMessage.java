package com.blr19c.blr.free.ai.api.vo;

import java.nio.file.Path;
import java.util.List;

/**
 * 文本生成模型信息
 */
public record ChatAiMessage(String chatMessage, List<Path> imageList) {
}
