package com.agent.core.difyai;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Dify 配置属性类 - 多应用支持
 */
@Getter
@Setter
@ConfigurationProperties(DifyProperties.CONFIG_PREFIX)
public class DifyProperties implements Serializable {
    public static final String CONFIG_PREFIX = "spring.ai.agent.dify";

    /**
     * 是否启用 Dify 集成
     */
    private boolean enabled = true;

    /**
     * 全局连接超时（毫秒）
     */
    private Long connectTimeout = 30000L;

    /**
     * 全局 Socket 超时（毫秒）
     */
    private Long socketTimeout = 60000L;

    /**
     * 多个 Dify 应用配置
     * Key: 应用标识（如 "customer-service", "data-analysis"）
     * Value: 应用具体配置
     */
    private Map<String, DifyAppConfig> apps = new HashMap<>();

    /**
     * 获取指定应用配置
     */
    public DifyAppConfig getApp(String appKey) {
        if (!enabled) {
            throw new IllegalStateException("Dify 集成未启用");
        }

        if (appKey == null || appKey.trim().isEmpty()) {
            throw new IllegalArgumentException("应用 Key 不能为空");
        }

        DifyAppConfig config = apps.get(appKey);
        if (config == null) {
            throw new IllegalArgumentException("未找到 Dify 应用配置: " + appKey);
        }

        if (!config.isEnabled()) {
            throw new IllegalStateException("Dify 应用已禁用: " + appKey);
        }

        return config;
    }

    /**
     * 单个应用配置
     */
    @Getter
    @Setter
    public static class DifyAppConfig implements Serializable {
        /**
         * 应用名称（描述性）
         */
        private String name;

        /**
         * Dify API Key
         */
        private String apiKey;

        /**
         * Dify Base URL
         */
        private String baseUrl;

        /**
         * 应用特定的连接超时（可选，覆盖全局配置）
         */
        private Long connectTimeout;

        /**
         * 应用特定的 Socket 超时（可选，覆盖全局配置）
         */
        private Long socketTimeout;

        /**
         * 是否启用此应用
         */
        private boolean enabled = true;
    }
}
