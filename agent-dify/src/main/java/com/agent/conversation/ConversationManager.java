package com.agent.conversation;




import com.agent.model.Conversation;
import com.agent.model.ConversationPayload;
import org.xiaowu.wpywebframework.authorization.context.UserContext;
import org.xiaowu.wpywebframework.common.model.PagerResult;

import java.util.List;

/**
 * @author wupy
 **/
public interface ConversationManager {

    /**
     * 新增会话
     *
     * @param authorization 用户信息
     * @param conversation  对话信息
     */
    Conversation insert(UserContext authorization, Conversation conversation);


    /**
     * 重命名会话
     *
     * @param userContext 用户信息
     * @param conversation  对话信息
     */
    Conversation rename(UserContext userContext, Conversation conversation);


    /**
     * 新增会话历史记录
     *
     * @param userContext 用户信息
     * @param   payload   对话内容
     * @param refresh       是否刷新对话内容
     */
    void addPayLoad(UserContext userContext, ConversationPayload payload, boolean refresh);

    /**
     * 获取对话信息
     *
     * @param conversationId 对话Id
     */
    Conversation getConversation(String conversationId);

    /**
     * 获取对话列表
     *
     * @param context 用户信息
     * @param topic         对话主题
     */
    List<Conversation> getConversationList(UserContext context, String topic);

    /**
     * 删除会话
     *
     * @param context 用户信息
     * @param payload  对话信息
     */
    Conversation delete(UserContext context, Conversation payload);

    /**
     * 获取对话列表
     *
     * @param context 用户信息
     * @param topic         对话主题
     * @param size          条数
     */
    List<Conversation> getLatestConversationList(UserContext context, String topic, int size);

    /**
     * 根据时间获取最近的10对话列表
     *
     * @param context   用户信息
     * @param conversationId  对话Id
     * @param oldestPayloadId 最早的聊天内容Id
     */
    PagerResult<ConversationPayload> getConversationPayloadList(UserContext context, String conversationId, String oldestPayloadId);

    /**
     * 获取最近的对话列表
     *
     * @param context  用户信息
     * @param conversationId 对话Id
     * @param size           条数
     */
    List<ConversationPayload> getLatestConversationPayloadList(UserContext context, String conversationId, int size);

}
