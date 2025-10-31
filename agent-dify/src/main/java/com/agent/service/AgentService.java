package com.agent.service;

import com.agent.model.Conversation;
import com.agent.model.ConversationPayload;
import com.agent.model.ConversationRequest;
import org.xiaowu.wpywebframework.authorization.context.UserContext;
import org.xiaowu.wpywebframework.common.model.PagerResult;
import reactor.core.publisher.Flux;

import java.util.List;

public interface AgentService {

    /**
     * 智能体对话
     */
    Flux<String> completions(UserContext authorization, ConversationRequest conversationRequest);

    /**
     * 获取会话信息
     *
     * @param conversationId 会话ID
     */
    Conversation getConversation(String conversationId);

    /**
     * 获取当前登录用户会话列表
     *
     * @param context 登录信息
     * @param topic         会话主题
     */
    List<Conversation> getConversationList(UserContext context, String topic);

    /**
     * 删除会话信息
     *
     */
    void delete(UserContext context, Conversation conversation);


    /**
     * 重命名会话信息
     *
     * @param conversation 会话信息
     */
    void rename(UserContext context, Conversation conversation);

    /**
     * 获取当前登录用户会话列表
     *
     * @param topic 会话主题
     * @param size  获取最近多少条数据
     */
    List<Conversation> getLatestConversationList(UserContext context, String topic, int size);

    /**
     * 获取当前登录用户会话列表
     */
    PagerResult<ConversationPayload> getConversationPayloadList(UserContext context, String conversationId, String oldestPayloadId);


    /**
     * 根据会话ID获取最近的聊天内容
     *
     * @param conversationId 会话Id
     * @param size           获取最近多少条数据
     */
    List<ConversationPayload> getLatestConversationPayloadList(UserContext context, String conversationId, int size);

}
