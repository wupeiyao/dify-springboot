package com.agent.core.difyai;

import com.agent.core.AgentRequest;
import com.agent.core.AgentResponse;
import com.alibaba.fastjson2.JSONObject;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.util.StringUtils;
import org.xiaowu.wpywebframework.core.utils.Y;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Dify API 客户端封装
 */
@Slf4j
@Builder
public class DifyAgentApi {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client;
    private final DifyProperties properties;

    public DifyAgentApi(OkHttpClient client, DifyProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    /**
     * 流式请求
     *
     * @param request 请求体
     */
    public Flux<AgentResponse> stream( AgentRequest request) {
        return Flux.create(sink -> Y.executor.submit(() -> {
            Response httpResponse = null;
            try {
                if (!properties.isEnabled()) {
                    sink.error(new IllegalStateException("Dify 集成未启用"));
                    return;
                }

                // 获取指定应用配置
                DifyProperties.DifyAppConfig appConfig = properties.getApp(request.getAppId());

                // 构建 Dify 请求体
                DifyRequestPayload payload = DifyRequestPayload.builder()
                        .query(request.getText())
                        .user(request.getUser())
                        .conversationId(request.getConversationId())
                        .token(request.getToken())
                        .enabled(true)
                        .build();
                String jsonBody = payload.toJson();

                Request httpRequest = new Request.Builder()
                        .url(appConfig.getBaseUrl() + "/chat-messages")
                        .addHeader("Authorization", "Bearer " + appConfig.getApiKey())
                        .addHeader("Content-Type", "application/json")
                        .post(RequestBody.create(jsonBody, JSON))
                        .build();
                httpResponse = client.newCall(httpRequest).execute();
                // 立即注册取消回调
                final Response finalResponse = httpResponse;
                sink.onDispose(() -> {
                    log.info("[Dify][{}] 流被释放", request.getAppId());
                    finalResponse.close();
                });
                // 检查响应状态
                if (!httpResponse.isSuccessful()) {
                    try (ResponseBody errorBody = httpResponse.body()) {
                        String body = errorBody != null ? errorBody.string() : "Unknown error";
                        sink.error(new IOException("Dify API 返回错误: " + body));
                    }
                    return;
                }

                ResponseBody body = httpResponse.body();
                if (body == null) {
                    sink.error(new IOException("Dify 返回空响应体"));
                    return;
                }

                // 使用 try-with-resources 自动管理 Reader 资源
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(body.byteStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        // 检查是否被取消
                        if (sink.isCancelled()) {
                            break;
                        }
                        // 跳过空行
                        if (line.trim().isEmpty()) {
                            continue;
                        }
                        // 处理 SSE 格式: data: {...}
                        if (line.startsWith("data:")) {
                            line = line.substring(5).trim();
                        }
                        // 处理结束标记
                        if ("[DONE]".equalsIgnoreCase(line)) {
                            sink.next(AgentResponse.end(request));
                            sink.complete();
                            break;
                        }

                        // 解析 JSON 数据块
                        try {
                            JSONObject chunk = JSONObject.parseObject(line);
                            String event = chunk.getString("event");
                            String answer = chunk.getString("answer");

                            // 处理文本内容
                            if (StringUtils.hasText(answer)) {
                                sink.next(AgentResponse.text(request, answer));
                            }
                            // 处理消息结束事件
                            if ("message_end".equals(event)) {
                                String conversationId = chunk.getString("conversation_id");
                                if (StringUtils.hasText(conversationId)) {
                                    request.setConversationId(conversationId);
                                }
                                sink.next(AgentResponse.end(request));
                                sink.complete();
                                break;
                            }

                            // 处理错误事件
                            if ("error".equals(event)) {
                                String errorMsg = chunk.getString("message");
                                log.error("[Dify][{}] 流式响应错误事件: {}", request.getAppId(), errorMsg);
                                sink.error(new IOException("Dify 返回错误: " + errorMsg));
                                break;
                            }

                        } catch (Exception e) {
                            log.warn("[Dify][{}] 流解析异常: line={}, error={}", request.getAppId(), line, e.getMessage());
                            // 继续处理下一行，不中断整个流
                            continue;
                        }
                    }

                    // 如果循环正常结束但没有明确的结束信号，手动发送结束
                    if (!sink.isCancelled()) {
                        sink.next(AgentResponse.end(request));
                        sink.complete();
                    }

                } // try-with-resources 会自动关闭 reader
            } catch (Exception e) {
                log.error("[Dify][{}] 流式请求异常: {}", request.getAppId(), e.getMessage(), e);
                if (!sink.isCancelled()) {
                    sink.error(e);
                }
            } finally {
                // 确保 HTTP 响应被关闭
                if (httpResponse != null) {
                    try {
                        httpResponse.close();
                    } catch (Exception e) {
                        log.warn("[Dify][{}] 关闭 HTTP 响应失败: {}", request.getAppId(), e.getMessage());
                    }
                }
            }
        }), FluxSink.OverflowStrategy.BUFFER);
    }

    public void delete( AgentRequest request) throws IOException {
        DifyProperties.DifyAppConfig appConfig = properties.getApp(request.getAppId());
        // 构建 Dify 请求体
        DifyRequestPayload payload = DifyRequestPayload.builder()
                .user(request.getUser())
                .conversationId(request.getConversationId())
                .enabled(true)
                .build();
        String jsonBody = payload.toJson();

        Request httpRequest = new Request.Builder()
                .url(appConfig.getBaseUrl() + "/chat-messages/" + request.getConversationId())
                .addHeader("Authorization", "Bearer " + appConfig.getApiKey())
                .addHeader("Content-Type", "application/json")
                .delete(RequestBody.create(jsonBody, JSON))
                .build();

        // 发送请求并获取响应
        try (Response httpResponse = client.newCall(httpRequest).execute()) {
            // 检查响应是否成功
            if (!httpResponse.isSuccessful()) {
                String body = Objects.requireNonNull(httpResponse.body()).string();
                log.error("delete httpResponse: {}", body);
                throw new IOException("Unexpected");
            }
            String body = Objects.requireNonNull(httpResponse.body()).string();
            log.info("delete httpResponse: {}", body);
        }
    }

    public void rename(AgentRequest request) throws IOException {
        DifyProperties.DifyAppConfig appConfig = properties.getApp(request.getAppId());
        // 构建 Dify 请求体
        DifyRequestPayload payload = DifyRequestPayload.builder()
                .user(request.getUser())
                .conversationId(request.getConversationId())
                .enabled(true)
                .build();
        String jsonBody = payload.toJson();

        Request httpRequest = new Request.Builder()
                .url(appConfig.getBaseUrl() + "/chat-messages/" + request.getConversationId() + "/name")
                .addHeader("Authorization", "Bearer " + appConfig.getApiKey())
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(jsonBody, JSON))
                .build();

        // 发送请求并获取响应
        try (Response httpResponse = client.newCall(httpRequest).execute()) {
            // 检查响应是否成功
            if (!httpResponse.isSuccessful()) {
                String body = Objects.requireNonNull(httpResponse.body()).string();
                log.error("rename httpResponse: {}", body);
                throw new IOException("Unexpected");
            }
            String body = Objects.requireNonNull(httpResponse.body()).string();
            log.info("rename httpResponse: {}", body);
        }
    }

}
