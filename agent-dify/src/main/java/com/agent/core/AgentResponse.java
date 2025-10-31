package com.agent.core;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author wupy
 **/
@Getter
@Setter
@Builder
public class AgentResponse implements Serializable {

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 会话id
     */
    private String conversationId;
    /**
     * 是否结束
     */
    private boolean isEnd;

    /**
     * 消息类型
     */
    private Type type;

    /**
     * 消息内容
     */
    private Object payload;


    public static AgentResponse text(AgentRequest request, Object payload) {
        return AgentResponse.builder()
                .requestId(request.getRequestId())
                .conversationId(request.getConversationId())
                .type(Type.text)
                .payload(payload)
                .build();
    }

    public static AgentResponse error(AgentRequest request, String payload) {
        return AgentResponse.builder()
                .requestId(request.getRequestId())
                .messageId(request.getMessageId())
                .conversationId(request.getConversationId())
                .type(Type.error)
                .payload(payload)
                .isEnd(true)
                .build();
    }

    public static AgentResponse end(AgentRequest request) {
        return AgentResponse.builder()
                .requestId(request.getRequestId())
                .conversationId(request.getConversationId())
                .isEnd(true)
                .type(Type.end)
                .build();
    }

    /**
     * @author wupy
     **/
    public enum Type {

        /**
         * 错误消息
         */
        error,
        /**
         * 音频消息
         */
        audio,
        /**
         * 文本消息
         */
        text,

        /**
         * 结束消息
         */
        end

    }
}
