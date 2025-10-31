package com.agent.model;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @description:
 * @author: xiaowu
 * @time: 2025/8/6 20:38
 */
@Getter
@Setter
public class Conversation implements Serializable {
    @Schema(description = "ID")
    private String id;

    @Schema(description = "对话ID")
    private String conversationId;

    @Schema(description = "应用id")
    private String appId;

    @Schema(description = "对话名称")
    private String name;

    @Schema(description = "对话主题")
    private String topic;

    @Schema(description = "对话分组")
    private ConversationGroup group;

    @Schema(description = "对话时间")
    private Date createTime;

    public static Conversation of(String topic, String requestId,String conversationId, String name) {
        Conversation conversation = new Conversation();
        conversation.setConversationId(conversationId);
        conversation.setId(requestId);
        conversation.setName(name);
        conversation.setTopic(topic);
        return conversation;
    }
}
