package com.blr19c.blr.free.ai.utils.client;

import io.netty.channel.ChannelOption;
import org.springframework.data.util.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * WebClient工具
 */
public class WebClientUtils {

    private static final Lazy<WebClient> WEB_CLIENT = Lazy.of(() -> {
        HttpClient httpClient = HttpClient.create()
                .compress(true)
                .responseTimeout(Duration.ofSeconds(30))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) Duration.ofSeconds(30).toMillis());
        return buildWebClient(httpClient);
    });

    private static final Lazy<WebClient> LONG_TIMEOUT_WEB_CLIENT = Lazy.of(() -> {
        HttpClient httpClient = HttpClient.create()
                .compress(true)
                .responseTimeout(Duration.ofMinutes(10))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) Duration.ofMinutes(1).toMillis());
        return buildWebClient(httpClient);
    });


    public static WebClient webClient() {
        return WEB_CLIENT.get();
    }


    public static WebClient longTimeoutWebClient() {
        return LONG_TIMEOUT_WEB_CLIENT.get();
    }

    private static WebClient buildWebClient(HttpClient httpClient) {
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(exchangeStrategies())
                .defaultHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br")
                .build();
    }


    private static ExchangeStrategies exchangeStrategies() {
        return ExchangeStrategies.builder().codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)).build();
    }
}
