package com.agent.provider;

import com.agent.core.difyai.DifyAgentApi;
import com.agent.core.difyai.DifyProperties;
import com.agent.core.AgentRequest;
import com.agent.core.AgentResponse;
import com.agent.core.spec.StringStreamResponseSpec;
import com.agent.model.Conversation;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xiaowu.wpywebframework.authorization.context.UserContext;
import reactor.core.publisher.Flux;

@Slf4j
@Component
public class DifyAgentProvider implements AgentProvider {

    private static final Logger logger = LoggerFactory.getLogger(DifyAgentProvider.class);

    private final DifyAgentApi api;

    public DifyAgentProvider(OkHttpClient client,
                             DifyProperties properties) {
        this.api = DifyAgentApi.builder()
                .client(client)
                .properties(properties)
                .build();
    }


    @Override
    public StringStreamResponseSpec stream(AgentRequest request) {
        try {
            return StringStreamResponseSpec.of(api.stream(request));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void rename(UserContext userContext, Conversation conversation) {
        try {
            AgentRequest agentRequest = AgentRequest.builder()
                    .appId(conversation.getAppId())
                    .user(userContext.getUserId())
                    .conversationId(conversation.getConversationId())
                    .build();
            api.rename(agentRequest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(UserContext userContext, Conversation conversation) {
        try {
            AgentRequest agentRequest = AgentRequest.builder()
                    .appId(conversation.getAppId())
                    .user(userContext.getUserId())
                    .conversationId(conversation.getConversationId())
                    .build();
            api.delete(agentRequest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
