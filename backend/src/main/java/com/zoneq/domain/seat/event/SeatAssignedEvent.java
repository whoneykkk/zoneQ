package com.zoneq.domain.seat.event;

public record SeatAssignedEvent(Long userId, String zone, int seatNumber) {}
