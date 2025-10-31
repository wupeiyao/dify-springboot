package com.agent.enums;

public enum ConversationParam {

    APP_ID("appId"),
    TEXT("text"),
    CONVERSATION_ID("conversationId"),
    TOKEN("token");

    private final String key;

    ConversationParam(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}
