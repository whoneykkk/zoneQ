package com.zoneq.domain.seat.service;

import com.zoneq.domain.seat.dto.SeatResponse;
import com.zoneq.domain.seat.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;

    @Transactional(readOnly = true)
    public List<SeatResponse> getSeats(String zone) {
        var seats = StringUtils.hasText(zone)
                ? seatRepository.findByZone(zone.toUpperCase())
                : seatRepository.findAll();

        return seats.stream()
                .map(SeatResponse::from)
                .toList();
    }
}
