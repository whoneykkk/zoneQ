package com.zoneq.domain.dashboard.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.assertj.core.api.Assertions.assertThat;

class SseEmitterRegistryTest {

    private SseEmitterRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new SseEmitterRegistry();
    }

    @Test
    void register_increasesSize() {
        registry.register("id1", new SseEmitter());
        assertThat(registry.size()).isEqualTo(1);
    }

    @Test
    void remove_decreasesSize() {
        registry.register("id1", new SseEmitter());
        registry.remove("id1");
        assertThat(registry.size()).isEqualTo(0);
    }

    @Test
    void broadcast_removesDeadEmitters() {
        SseEmitter deadEmitter = new SseEmitter();
        deadEmitter.complete();
        registry.register("dead", deadEmitter);

        registry.broadcast("test data");

        assertThat(registry.size()).isEqualTo(0);
    }

    @Test
    void broadcast_toEmptyRegistry_doesNotThrow() {
        registry.broadcast("test data");
        assertThat(registry.size()).isEqualTo(0);
    }
}
