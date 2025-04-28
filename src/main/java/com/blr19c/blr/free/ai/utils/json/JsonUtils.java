package com.blr19c.blr.free.ai.utils.json;

import com.blr19c.blr.free.ai.config.spring.SpringBeanUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;

/**
 * json工具
 */
public class JsonUtils {

    @SneakyThrows
    public static JsonNode readJsonNode(String json) {
        return SpringBeanUtils.getBean(ObjectMapper.class).readTree(json);
    }

    @SneakyThrows
    public static ArrayNode readArrayNode(String json) {
        return (ArrayNode) readJsonNode(json);
    }

    public static ObjectNode createObjectNode() {
        return SpringBeanUtils.getBean(ObjectMapper.class).createObjectNode();
    }

    @SneakyThrows
    public static String toJsonString(Object obj) {
        return SpringBeanUtils.getBean(ObjectMapper.class).writeValueAsString(obj);
    }

    @SneakyThrows
    public static <T> T readObject(String json, Class<T> clazz) {
        return SpringBeanUtils.getBean(ObjectMapper.class).readValue(json, clazz);
    }

    @SneakyThrows
    public static <T> T readObject(String json, TypeReference<T> typeReference) {
        return SpringBeanUtils.getBean(ObjectMapper.class).readValue(json, typeReference);
    }
}
