package com.zoneq.domain.dashboard.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DashboardScheduler {

    private final DashboardService dashboardService;

    @Scheduled(fixedRate = 5000)
    @Async("sseAsyncExecutor")
    public void broadcast() {
        try {
            dashboardService.broadcastRealtime();
        } catch (Exception e) {
            log.warn("SSE broadcast failed: {}", e.getMessage());
        }
    }
}
