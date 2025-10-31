package com.agent.model;

import lombok.*;

import java.io.Serializable;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationRequest implements Serializable {

    private String requestId;

    /**
     * 主题
     */
    private String topic;

    /**
     * 参数
     */
    private Map<String, Object> parameter;
}
