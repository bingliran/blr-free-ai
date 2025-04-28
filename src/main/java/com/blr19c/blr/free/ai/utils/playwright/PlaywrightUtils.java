package com.blr19c.blr.free.ai.utils.playwright;

import com.blr19c.blr.free.ai.utils.application.ApplicationUtils;
import com.blr19c.blr.free.ai.utils.json.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.SameSiteAttribute;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Lazy;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * Playwright工具
 */
@Slf4j
public class PlaywrightUtils {

    private static final Lazy<List<Cookie>> COOKIE_LAZY = Lazy.of(() -> {
        Path cookiePath = ApplicationUtils.getLocalDir().resolve("cookies");
        log.info("初始化Playwright-Cookies, path:{}", cookiePath);
        try (Stream<Path> walk = Files.walk(cookiePath)) {
            List<Cookie> list = walk
                    .filter(Files::isRegularFile)
                    .filter(p -> p.normalize().toString().endsWith(".json"))
                    .map(PlaywrightUtils::parseCookie)
                    .flatMap(Collection::stream)
                    .toList();
            log.info("初始化Playwright-Cookies, size:{}", list.size());
            return list;
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    });

    @SneakyThrows
    private static List<Cookie> parseCookie(Path cookiePath) {
        String cookieContent = Files.readString(cookiePath, StandardCharsets.UTF_8);
        List<JsonNode> cookies = JsonUtils.readObject(cookieContent, new TypeReference<>() {
        });
        return cookies.stream().map(node -> {
            Cookie cookie = new Cookie(node.get("name").asText(), node.get("value").asText());
            cookie.setDomain(node.get("domain").asText());
            cookie.setPath(node.get("path").asText());
            cookie.setExpires(node.get("expirationDate").asDouble());
            cookie.setHttpOnly(node.get("httpOnly").asBoolean());
            cookie.setSecure(node.get("secure").asBoolean());
            if (node.get("sameSite").asText().equals("no_restriction")) {
                cookie.setSameSite(SameSiteAttribute.LAX);
            } else {
                cookie.setSameSite(SameSiteAttribute.NONE);
            }
            return cookie;
        }).toList();
    }

    public static void acceptPage(BiConsumer<PlaywrightInfo, Page> consumer) {
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions().setHeadless(true);
        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(launchOptions);
             BrowserContext browserContext = browser.newContext();
             Page page = browserContext.newPage()) {
            browserContext.addCookies(COOKIE_LAZY.get());
            consumer.accept(new PlaywrightInfo(playwright, browser, browserContext), page);
        }
    }

    public record PlaywrightInfo(
            Playwright playwright,
            Browser browser,
            BrowserContext browserContext
    ) {
    }
}


