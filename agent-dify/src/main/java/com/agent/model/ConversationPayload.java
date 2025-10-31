package com.agent.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author wupy
 **/
@Getter
@Setter
public class ConversationPayload implements Serializable {

    @Schema(description = "Id")
    private String id;

    @Schema(description = "对话Id")
    private String conversationId;

    @Schema(description = "请求Id")
    private String requestId;

    @Schema(description = "消息Id")
    private String messageId;

    @Schema(description = "对话主题")
    private String topic;


    @Schema(description = "对话是否成功")
    private Boolean ok = true;

    @Schema(description = "对话内容")
    private String content;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "备注信息")
    private String description;

    @Schema(description = "对话时间")
    private Date createTime;

    public static ConversationPayload Q(Conversation conversation, String topic, String requestId, String content) {
        ConversationPayload payload = new ConversationPayload();
        payload.conversationId = conversation.getId();
        payload.topic = topic;
        payload.requestId = requestId;
        payload.ok = true;
        payload.content = content;
        return payload;
    }

    public static ConversationPayload A(Conversation conversation, String topic, String requestId, String messageId, String content, boolean ok) {
        ConversationPayload payload = new ConversationPayload();
        payload.conversationId = conversation.getId();
        payload.topic = topic;
        payload.requestId = requestId;
        payload.messageId = messageId;
        payload.ok = ok;
        payload.content = content;
        return payload;
    }

}
