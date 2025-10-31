package com.agent.core.difyai;

import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 自动装配类：当 spring.ai.agent.provider=dify 时加载
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(DifyProperties.class)
@RequiredArgsConstructor
public class DifyAgentConfiguration {

    private final DifyProperties properties;

    @Bean
    public OkHttpClient difyHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(properties.getConnectTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(properties.getSocketTimeout(), TimeUnit.MILLISECONDS)
                .build();
    }

    @Bean
    public DifyAgentApi difyAgentApi(OkHttpClient client) {
        return new DifyAgentApi(client, properties);
    }
}
