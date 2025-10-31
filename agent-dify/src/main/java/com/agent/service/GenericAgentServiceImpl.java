package com.agent.service;

import com.agent.conversation.ConversationManager;
import com.agent.enums.ConversationParam;
import com.agent.enums.FluxType;
import com.agent.enums.MessageType;
import com.agent.provider.AgentProvider;
import com.agent.core.AgentRequest;
import com.agent.entity.ConversationPayloadEntity;
import com.agent.model.*;
import com.agent.repository.ConversationPayloadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xiaowu.wpywebframework.authorization.context.UserContext;
import org.xiaowu.wpywebframework.common.model.PagerResult;
import org.xiaowu.wpywebframework.core.utils.Y;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;

import java.io.IOException;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @description:
 * @author: xiaowu
 * @time: 2025/10/8 21:30
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class GenericAgentServiceImpl implements AgentService {

    private final ConversationManager conversationManager;

    private final AgentProvider provider;

    private final ConversationPayloadRepository conversationPayloadRepository;

    /**
     * 对话接口 流式输出版本
     */
    @Override
    public Flux<String> completions(UserContext authorization, ConversationRequest conversationRequest) {
        if (conversationRequest.getParameter() == null) {
            return Flux.just(MessageType.PARAM_EMPTY.format(), MessageType.DONE.format());
        }
        String appId = (String) conversationRequest.getParameter().get(ConversationParam.APP_ID.key());
        String conversationId = (String) conversationRequest.getParameter()
                .get(ConversationParam.CONVERSATION_ID.key());
        String text = (String) Optional.of(conversationRequest.getParameter())
                .map(p -> p.get(ConversationParam.TEXT.key()))
                .orElse("");
        String requestId = conversationRequest.getRequestId();
        String token = (String) conversationRequest.getParameter()
                .get(ConversationParam.TOKEN.key());
        // 保存用户请求消息（Q）
        if (authorization == null) {
            return Flux.just(MessageType.UNAUTHORIZED.format(), MessageType.DONE.format());
        }
        Conversation conversation = conversationManager.insert(authorization, Conversation.of(conversationRequest.getTopic(), requestId,conversationId, text));
        ConversationPayloadEntity userMessage = buildUserMessage(
                conversation.getConversationId(), conversation.getId(), conversationRequest.getTopic(),
                text, authorization, appId);
        try {
            conversationPayloadRepository.insert(userMessage);
        } catch (Exception e) {
            return Flux.just(MessageType.SAVE_FAILED.format(), MessageType.DONE.format());
        }
        AgentRequest agentRequest = AgentRequest.builder()
                .requestId(requestId)
                .conversationName(conversationRequest.getTopic())
                .conversationId(conversation.getConversationId())
                .user(authorization.getUserId())
                .appId(appId)
                .token(token)
                .text(text)
                .streaming(true)
                .metadata(conversationRequest.getParameter())
                .build();
        StringBuilder contentBuilder = new StringBuilder();
        AtomicBoolean hasError = new AtomicBoolean(false);
        AtomicReference<String> errorMessage = new AtomicReference<>("");
        // 发起流式请求
        return provider.stream(agentRequest).content()
                .map(response -> {
                    switch (response.getType()) {
                        case text:
                            String chunk = response.getPayload().toString();
                            contentBuilder.append(chunk);
                            return chunk;
                        case error:
                            String errMsg = response.getPayload().toString();
                            hasError.set(true);
                            errorMessage.set(errMsg);
                            return MessageType.ERROR.format(errMsg);
                        case end:
                            String difyConvId = response.getConversationId();
                            if (StringUtils.hasText(difyConvId)) {
                                try {
                                    conversation.setConversationId(difyConvId);
                                    conversationManager.rename(authorization, conversation);
                                } catch (Exception e) {
                                    log.error(e.getMessage());
                                }
                            }
                            return MessageType.DONE.format();
                        default:
                            return "";
                    }
                })
                .doFinally(signalType -> {
                    CompletableFuture.runAsync(() -> {
                        try {
                            String finalContent = contentBuilder.toString();
                            FluxType type;
                            if (signalType == SignalType.ON_COMPLETE && !hasError.get()) {
                                type = FluxType.COMPLETED;
                            } else if (signalType == SignalType.CANCEL) {
                                type = FluxType.CANCELLED;
                            } else if (hasError.get()) {
                                type = FluxType.FAILED;
                            } else {
                                type = FluxType.UNKNOWN;
                            }
                            String status = type.getStatus();
                            boolean ok = type.isOk();
                            String description = type.getDescription();
                            ConversationPayloadEntity aiMessage = buildAiMessage(
                                    conversation.getConversationId(),
                                    conversation.getId(),
                                    conversationRequest.getTopic(),
                                    finalContent,
                                    authorization.getUserId(),
                                    authorization.getUsername(),
                                    status,
                                    ok,
                                    description,
                                    appId
                            );
                            conversationPayloadRepository.insert(aiMessage);
                        } catch (Exception e) {
                            log.error(e.getMessage());
                        }
                    }, Y.executor).exceptionally(ex -> {
                        log.error(ex.getMessage(), ex);
                        return null;
                    });
                })
                .timeout(Duration.ofMinutes(5))
                .onErrorResume(e -> {
                    log.error(e.getMessage(), e);
                    String userFriendlyMsg;
                    if (e instanceof TimeoutException) {
                        userFriendlyMsg = MessageType.TIMEOUT.format();
                    } else if (e instanceof IOException) {
                        userFriendlyMsg = MessageType.NETWORK_ERROR.format();
                    } else {
                        userFriendlyMsg = MessageType.SYSTEM_ERROR.format(e.getMessage());
                    }
                    return Flux.just(userFriendlyMsg, MessageType.DONE.format());
                });
    }

    // 构建用户消息
    private ConversationPayloadEntity buildUserMessage(
            String conversationId, String requestId, String topic,
            String content, UserContext authorization, String appId) {

        ConversationPayloadEntity message = new ConversationPayloadEntity();
        message.setId(UUID.randomUUID().toString());
        message.setConversationId(conversationId);
        message.setRequestId(requestId);
        message.setMessageId(UUID.randomUUID().toString());
        message.setTopic(topic);
        message.setContent(content);
        message.setStatus("sent");
        message.setOk(true);
        message.setRole(ConversationPayloadRole.Q);
        message.setCreatorId(authorization.getUserId());
        message.setCreatorName(authorization.getUsername());
        message.setCreateTime(new Date());
        message.setDescription("appId:" + appId);

        return message;
    }

    // 构建AI消息
    private ConversationPayloadEntity buildAiMessage(
            String conversationId, String requestId, String topic,
            String content, String userId, String username,
            String status, boolean ok, String description, String appKey) {

        ConversationPayloadEntity message = new ConversationPayloadEntity();
        message.setId(UUID.randomUUID().toString());
        message.setConversationId(conversationId);
        message.setRequestId(requestId);
        message.setMessageId(UUID.randomUUID().toString());
        message.setTopic(topic);
        message.setContent(content);
        message.setStatus(status);
        message.setOk(ok);
        message.setRole(ConversationPayloadRole.A);
        message.setCreatorId(userId);
        message.setCreatorName("AI");
        message.setCreateTime(new Date());

        if (description != null) {
            message.setDescription(description + " | appKey:" + appKey);
        } else {
            message.setDescription("appKey:" + appKey);
        }

        return message;
    }

    @Override
    public Conversation getConversation(String conversationId) {
        try {
            if (conversationId == null || conversationId.trim().isEmpty()) {
                throw new IllegalArgumentException("会话ID不能为空");
            }
            Conversation conversation = conversationManager.getConversation(conversationId);
            if (conversation == null) {
                throw new IllegalArgumentException("会话不存在");
            }
            return conversation;
        } catch (Exception e) {
            log.error("获取会话信息失败: conversationId={}, error={}", conversationId, e.getMessage(), e);
            throw new RuntimeException("获取会话信息失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Conversation> getConversationList(UserContext context, String topic) {
        try {
            return conversationManager.getConversationList(context, topic);
        } catch (Exception e) {
            log.error("获取会话列表失败: userId={}, topic={}, error={}",
                    context != null ? context.getUserId() : "unknown", topic, e.getMessage(), e);
            throw new RuntimeException("获取会话列表失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(UserContext context, Conversation con) {
        try {
            if (con.getConversationId() == null || con.getConversationId().trim().isEmpty()) {
                throw new IllegalArgumentException("会话ID不能为空");
            }
            Conversation payload = new Conversation();
            payload.setId(con.getConversationId());
            // 删除会话信息
            Conversation conversation = conversationManager.delete(context, payload);
            if (conversation == null) {
                throw new IllegalArgumentException("会话不存在或已被删除");
            }
            // 同步删除人工智能平台会话内容
            provider.delete(context,conversation);
            log.info("删除会话成功: conversationId={}, userId={}", conversation.getConversationId(), context.getUserId());
        } catch (IllegalArgumentException e) {
            log.error("删除会话失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("删除会话失败: conversationId={}, error={}", con.getConversationId(), e.getMessage(), e);
            throw new RuntimeException("删除会话失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void rename(UserContext context, Conversation conversation) {
        try {
            if (conversation == null || conversation.getId() == null || conversation.getId().trim().isEmpty()) {
                throw new IllegalArgumentException("会话ID不能为空");
            }
            if (conversation.getName() == null || conversation.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("会话名称不能为空");
            }
            conversation = conversationManager.rename(context, conversation);
            if (conversation == null) {
                throw new IllegalArgumentException("会话不存在或重命名失败");
            }
            // 同步重命名人工智能平台会话内容
            provider.rename(context, conversation);
            log.info("重命名会话成功: conversationId={}, newName={}, userId={}",
                    conversation.getId(), conversation.getName(), context.getUserId());
        } catch (IllegalArgumentException e) {
            log.error("重命名会话失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("重命名会话失败: conversationId={}, error={}",
                    conversation != null ? conversation.getId() : "unknown", e.getMessage(), e);
            throw new RuntimeException("重命名会话失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Conversation> getLatestConversationList(UserContext context, String topic, int size) {
        try {
            if (size <= 0) {
                throw new IllegalArgumentException("获取数量必须大于0");
            }
            if (size > 100) {
                log.warn("获取会话列表数量过大，已限制为100: requestSize={}", size);
                size = 100;
            }
            return conversationManager.getLatestConversationList(context, topic, size);
        } catch (IllegalArgumentException e) {
            log.error("获取最新会话列表失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("获取最新会话列表失败: userId={}, topic={}, size={}, error={}",
                    context != null ? context.getUserId() : "unknown", topic, size, e.getMessage(), e);
            throw new RuntimeException("获取最新会话列表失败: " + e.getMessage(), e);
        }
    }

    @Override
    public PagerResult<ConversationPayload> getConversationPayloadList(UserContext context, String conversationId,
            String oldestPayloadId) {
        try {
            if (conversationId == null || conversationId.trim().isEmpty()) {
                throw new IllegalArgumentException("会话ID不能为空");
            }
            return conversationManager.getConversationPayloadList(context, conversationId, oldestPayloadId);
        } catch (IllegalArgumentException e) {
            log.error("获取会话消息列表失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("获取会话消息列表失败: conversationId={}, error={}", conversationId, e.getMessage(), e);
            throw new RuntimeException("获取会话消息列表失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ConversationPayload> getLatestConversationPayloadList(UserContext context, String conversationId,
                                                                      int size) {
        try {
            if (conversationId == null || conversationId.trim().isEmpty()) {
                throw new IllegalArgumentException("会话ID不能为空");
            }
            if (size <= 0) {
                throw new IllegalArgumentException("获取数量必须大于0");
            }
            if (size > 100) {
                log.warn("获取消息列表数量过大，已限制为100: requestSize={}", size);
                size = 100;
            }
            return conversationManager.getLatestConversationPayloadList(context, conversationId, size);
        } catch (IllegalArgumentException e) {
            log.error("获取最新会话消息列表失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("获取最新会话消息列表失败: conversationId={}, size={}, error={}",
                    conversationId, size, e.getMessage(), e);
            throw new RuntimeException("获取最新会话消息列表失败: " + e.getMessage(), e);
        }
    }
}
