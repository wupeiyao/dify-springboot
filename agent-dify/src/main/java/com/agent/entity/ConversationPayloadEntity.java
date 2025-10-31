package com.agent.entity;

import com.agent.model.ConversationPayloadRole;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.JdbcType;
import org.xiaowu.wpywebframework.core.generic.GenericEntity;

import java.util.Date;

/**
 * 对话历史表实体类
 * 对应表名：chatbox_conversation_payload
 * 用于存储每次对话的请求、响应记录及其状态等信息
 *
 * @author xiaowu
 * @date 2025/8/5 23:40
 */
@Getter
@Setter
@TableName(value = "chatbox_conversation_payload", autoResultMap = true)
public class ConversationPayloadEntity extends GenericEntity<String> {

    /**
     * 主键 ID
     */
    @Override
    public String getId() {
        return super.getId();
    }

    /**
     * 会话 ID
     * 对应数据库字段：conversation_id
     * 表示该消息所属的会话
     */
    @TableField(value = "conversation_id")
    private String conversationId;

    /**
     * 请求 ID
     * 对应数据库字段：request_id
     * 可用于追踪一次完整的请求调用
     */
    @TableField(value = "request_id")
    private String requestId;

    /**
     * 消息 ID
     * 对应数据库字段：message_id
     * 可标识具体某一条对话消息
     */
    @TableField(value = "message_id")
    private String messageId;

    /**
     * 对话主题
     * 对应数据库字段：topic
     * 可选字段，表示该条消息所属主题（与 ConversationEntity 的 topic 可对应）
     */
    @TableField(value = "topic")
    private String topic;

    /**
     * 消息内容
     * 对应数据库字段：content
     * 存储用户或 AI 的消息正文，支持较长文本
     */
    @TableField(value = "content", jdbcType = JdbcType.LONGVARCHAR)
    private String content;


    /**
     * 消息状态
     * 对应数据库字段：status
     * 可用于表示消息处理状态（如完成、失败等）
     */
    @TableField(value = "status")
    private String status;

    /**
     * 是否成功
     */
    @TableField(value = "ok")
    private Boolean ok;


    /**
     * 消息描述
     * 对应数据库字段：description
     * 记录备注信息或异常说明
     */
    @TableField(value = "description")
    private String description;

    /**
     * 创建人 ID
     * 对应数据库字段：creator_id
     * 创建后不可修改
     */
    @TableField(value = "creator_id", updateStrategy = FieldStrategy.NEVER)
    private String creatorId;

    /**
     * 创建人名称
     * 对应数据库字段：creator_name
     * 创建后不可修改
     */
    @TableField(value = "creator_name", updateStrategy = FieldStrategy.NEVER)
    private String creatorName;


    /**
     * 消息角色
     * 对应数据库字段：role
     * 标识该条消息的角色，如用户提问(Q)、AI 回答(A)
     */
    @TableField(value = "role")
    private ConversationPayloadRole role;


    private Date createTime;
}

