package com.agent.core.spec;

import com.agent.core.AgentResponse;
import reactor.core.publisher.Flux;

public interface StreamResponseSpec {

    Flux<AgentResponse> content();
}
