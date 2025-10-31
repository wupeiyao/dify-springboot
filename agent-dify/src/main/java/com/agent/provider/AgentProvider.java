package com.agent.provider;

import com.agent.core.AgentRequest;
import com.agent.core.AgentResponse;
import com.agent.core.spec.StringStreamResponseSpec;
import com.agent.model.Conversation;
import org.xiaowu.wpywebframework.authorization.context.UserContext;
import reactor.core.publisher.Flux;

public interface AgentProvider {

    StringStreamResponseSpec stream(AgentRequest request);

    void rename(UserContext userContext, Conversation conversation);

    void delete(UserContext userContext, Conversation conversation);


}
