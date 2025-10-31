package com.agent.core.difyai;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
public class DifyRequestPayload {

    /** Dify 应用输入参数 */
    @Builder.Default
    @JSONField(name = "inputs")
    private Map<String, Object> inputs = new HashMap<>();

    /** 主查询文本（对话应用时放在顶层） */
    @JSONField(name = "query")
    private String query;

    /** 响应模式（如 streaming / blocking） */
    @Builder.Default
    @JSONField(name = "response_mode")
    private String responseMode = "streaming";

    /** 用户 ID */
    @JSONField(name = "user")
    private String user;

    /** 会话 ID，可为空 */
    @JSONField(name = "conversation_id")
    private String conversationId;

    /** 后端访问令牌*/
    @JSONField(name = "token")
    private String token;

    /** 是否启用 Dify 功能（来自配置） */
    @Builder.Default
    @JSONField(serialize = false)
    private boolean enabled = true;

    /**
     * 构建最终请求体 JSON 字符串
     */
    public String toJson() {
        if (!enabled) {
            throw new IllegalStateException("Dify 集成未启用");
        }
        inputs.putIfAbsent("query", query);
        inputs.putIfAbsent("metedata", "chat-request");
        return JSONObject.toJSONString(this);
    }
}
