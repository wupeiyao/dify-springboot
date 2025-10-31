package com.agent.controller;


import com.agent.model.Conversation;
import com.agent.model.ConversationPayload;
import com.agent.model.ConversationRequest;
import com.agent.service.GenericAgentServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.xiaowu.wpywebframework.authorization.context.AuthorizationContext;
import org.xiaowu.wpywebframework.authorization.context.UserContext;
import org.xiaowu.wpywebframework.common.model.PagerResult;
import org.xiaowu.wpywebframework.core.utils.Result;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * @description:
 * @author: xiaowu
 * @time: 2025/10/8 21:30
 */

@RestController
@Tag(name = "Agent相关接口")
@RequestMapping("/ai/agent")
@RequiredArgsConstructor
public class AgentController {
    private final GenericAgentServiceImpl genericAgentService;

    @Operation(summary = "智能体对话")
    @PostMapping(value = "/v1/chat/completions", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> completion(@RequestBody ConversationRequest conversationRequest) {
        //模拟已经用户登录，存储信息到上下文会话里面
        UserContext userContext = new UserContext();
        userContext.setUserId("1921681211");
        userContext.setUsername("小屋");
        userContext.setToken("xiaowuxiaowuxiaowu");
        return genericAgentService.completions( userContext,conversationRequest);
    }

    /**
     * 获取会话信息
     */
    @GetMapping("/conversation/info")
    @Operation(summary = "获取会话信息")
    public Result<Conversation> getConversation(@Parameter(description = "会话Id") @RequestParam String conversationId) {
        Conversation conversation = genericAgentService.getConversation(conversationId);
        return Result.success(conversation);
    }

    /**
     * 获取当前登录用户会话列表
     */
    @GetMapping("/conversation/getList")
    @Operation(summary = "获取当前登录用户会话列表")
    public Result<List<Conversation>> getConversationList(@Parameter(description = "会话主题") @RequestParam String topic) {
        List<Conversation> conversationList = genericAgentService.getConversationList(AuthorizationContext.getContext(),
                topic);
        return Result.success(conversationList);
    }

    /**
     * 删除会话信息
     */
    @DeleteMapping("/conversation/delete")
    @Operation(summary = "删除会话信息")
    public Result<String> delete(@RequestBody Conversation conversation) {
        genericAgentService.delete(AuthorizationContext.getContext(), conversation);
        return Result.success("删除成功");
    }

    /**
     * 重命会话信息
     */
    @PostMapping("/conversation/rename")
    @Operation(summary = "重命名会话信息")
    public Result<String> rename(@RequestBody Conversation conversation) {
        genericAgentService.rename(AuthorizationContext.getContext(), conversation);
        return Result.success("重命名成功");
    }

    /**
     * 获取当前登录用户获取最新会话列表
     */
    @GetMapping("/conversation/getLatestList")
    @Operation(summary = "获取当前登录用户获取最新会话列表")
    public Result<List<Conversation>> getLatestConversationList(
            @Parameter(description = "会话主题") @RequestParam String topic,
            @Parameter(description = "获取最近多少条数据") @RequestParam int size) {
        List<Conversation> latestConversationList = genericAgentService
                .getLatestConversationList(AuthorizationContext.getContext(), topic, size);
        return Result.success(latestConversationList);
    }

    /**
     * 根据会话ID获取聊天内容
     */
    @GetMapping("/conversation/payload/getList")
    @Operation(summary = "根据会话ID获取聊天内容")
    public Result<PagerResult<ConversationPayload>> getConversationPayloadList(
            @Parameter(description = "会话ID") @RequestParam String conversationId,
            @Parameter(description = "会话时间") @RequestParam(required = false) String oldestPayloadId) {
        PagerResult<ConversationPayload> conversationPayloadPagerResult = genericAgentService
                .getConversationPayloadList(AuthorizationContext.getContext(), conversationId, oldestPayloadId);
        return Result.success(conversationPayloadPagerResult);
    }

    /**
     * 根据会话ID获取最近的聊天内容
     */
    @GetMapping("/conversation/payload/getLatestList")
    @Operation(summary = "根据会话ID获取最近的聊天内容")
    public Result<List<ConversationPayload>> getLatestConversationPayloadList(
            @Parameter(description = "会话ID") @RequestParam String conversationId,
            @Parameter(description = "获取最近多少条数据") @RequestParam int size) {
        List<ConversationPayload> latestConversationPayloadList = genericAgentService
                .getLatestConversationPayloadList(AuthorizationContext.getContext(), conversationId, size);
        return Result.success(latestConversationPayloadList);
    }
}
