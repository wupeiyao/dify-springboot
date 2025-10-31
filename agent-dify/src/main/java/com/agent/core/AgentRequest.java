package com.agent.core;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: xiaowu
 * @time: 2025/8/6 22:44
 */
@Getter
@Setter
@Builder
public class AgentRequest implements Serializable {

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 大模型消息ID
     */
    private String messageId;

    /**
     * 是否流式返回
     */
    private boolean streaming;

    /**
     * 会话id
     */
    private String conversationId;

    /**
     * 会话名称
     */
    private String conversationName;
    /**
     * 用户标识，用于定义终端用户的身份，方便检索、统计。 由开发者定义规则，需保证用户标识在应用内唯一。服务 API 不会共享 WebApp 创建的对话。
     */
    private String user;

    private String token;

    private String appId;


    /**
     * 对话输入
     */
    private String text;

    /**
     * 扩展信息
     */
    private Map<String, Object> metadata;

    /**
     * 附件信息
     */
    private List<Attachment> attachments;

    @Setter
    @Getter
    public static class Attachment implements Serializable {

        /**
         * 附件地址
         */
        private String url;

        /**
         * 附件名称
         */
        private String name;
    }
}
