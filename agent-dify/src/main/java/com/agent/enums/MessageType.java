package com.agent.enums;

public enum MessageType {

    PARAM_EMPTY("[错误]", "请求参数不能为空"),
    UNAUTHORIZED("[错误]", "未授权访问"),
    SAVE_FAILED("[系统错误]", "保存消息失败"),
    NETWORK_ERROR("[网络错误]", "连接异常，请检查网络"),
    TIMEOUT("[超时]", "请求处理超时，请稍后重试"),
    SYSTEM_ERROR("[系统错误]", ""), // 描述动态填充
    ERROR("error", ""),            // 用于 Dify 返回的错误内容
    DONE("[DONE]", "");

    private final String prefix;
    private final String defaultMessage;

    MessageType(String prefix, String defaultMessage) {
        this.prefix = prefix;
        this.defaultMessage = defaultMessage;
    }

    public String format() {
        return prefix + " " + defaultMessage;
    }

    public String format(String dynamicMessage) {
        return prefix + " " + dynamicMessage;
    }
}
