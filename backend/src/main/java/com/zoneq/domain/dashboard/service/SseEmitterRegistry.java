package com.zoneq.domain.dashboard.service;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterRegistry {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public void register(String id, SseEmitter emitter) {
        emitters.put(id, emitter);
    }

    public void remove(String id) {
        emitters.remove(id);
    }

    public void broadcast(Object data) {
        List<String> deadIds = new ArrayList<>();
        emitters.forEach((id, emitter) -> {
            try {
                emitter.send(SseEmitter.event().data(data));
            } catch (Exception e) {
                deadIds.add(id);
                emitter.complete();
            }
        });
        deadIds.forEach(emitters::remove);
    }

    public int size() {
        return emitters.size();
    }
}
