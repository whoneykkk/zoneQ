package com.zoneq.domain.dashboard.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DashboardSchedulerTest {

    @Mock private DashboardService dashboardService;

    @InjectMocks
    private DashboardScheduler dashboardScheduler;

    @Test
    void broadcast_callsBroadcastRealtime() {
        dashboardScheduler.broadcast();
        verify(dashboardService).broadcastRealtime();
    }
}
