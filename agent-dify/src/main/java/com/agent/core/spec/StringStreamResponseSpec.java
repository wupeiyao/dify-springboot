package com.agent.core.spec;

import com.agent.core.AgentResponse;
import reactor.core.publisher.Flux;

public class StringStreamResponseSpec implements StreamResponseSpec {

    public static final StringStreamResponseSpec EMPTY_SPEC = new StringStreamResponseSpec(Flux.empty());

    private final Flux<AgentResponse> content;

    private StringStreamResponseSpec(Flux<AgentResponse> content) {
        this.content = content;
    }

    public static StringStreamResponseSpec of(Flux<AgentResponse> content) {
        return new StringStreamResponseSpec(content);
    }

    public Flux<AgentResponse> content() {
        return content;
    }
}
