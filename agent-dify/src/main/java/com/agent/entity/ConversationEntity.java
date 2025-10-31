package com.agent.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.xiaowu.wpywebframework.core.generic.GenericEntity;

import java.util.Date;

/**
 * @description:
 * @author: xiaowu
 * @time: 2025/8/5 23:26
 */

@Getter
@Setter
@TableName(value = "chatbox_conversation")
public class ConversationEntity extends GenericEntity<String> {

    /**
     * 主键 ID
     */
    @Override
    public String getId() {
        return super.getId();
    }

    /**
     * 会话唯一标识
     * 对应数据库字段：conversation_id
     * 用于标识一段对话，在系统中逻辑唯一
     */
    @TableField("conversation_id")
    private String conversationId;

    /**
     * 会话名称
     * 对应数据库字段：name
     * 用户可自定义的对话标题，便于区分会话
     */
    @TableField(value = "name")
    private String name;

    /**
     * 会话主题
     * 对应数据库字段：topic
     * 可用于记录当前对话的大致内容方向（例如：技术、生活等）
     */
    @TableField(value = "topic")
    private String topic;

    /**
     * 创建人 ID
     * 对应数据库字段：creator_id
     * 用于记录是谁发起的对话，创建后不可更新
     */
    @TableField(value = "creator_id", updateStrategy = FieldStrategy.NEVER)
    private String creatorId;

    /**
     * 创建人名称
     * 对应数据库字段：creator_name
     * 创建人昵称或真实姓名，创建后不可更新
     */
    @TableField(value = "creator_name", updateStrategy = FieldStrategy.NEVER)
    private String creatorName;

    private Date createTime;

}
