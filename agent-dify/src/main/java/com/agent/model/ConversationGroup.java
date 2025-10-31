package com.agent.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ConversationGroup implements Serializable {

    @Schema(description = "对话分组ID")
    private String groupId;

    @Schema(description = "对话分组名称")
    private String groupName;

    public static ConversationGroup of(String groupId, String groupName) {
        ConversationGroup group = new ConversationGroup();
        group.setGroupId(groupId);
        group.setGroupName(groupName);
        return group;
    }
}
