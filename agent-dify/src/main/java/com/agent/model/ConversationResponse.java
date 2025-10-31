package com.agent.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Builder
public class ConversationResponse implements Serializable {

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
    private String type;

    /**
     * 消息内容
     */
    private Object payload;

}
