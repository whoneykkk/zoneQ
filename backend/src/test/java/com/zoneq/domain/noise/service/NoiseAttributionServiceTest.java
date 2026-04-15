package com.zoneq.domain.noise.service;

import com.zoneq.domain.noise.domain.CalibrationMap;
import com.zoneq.domain.noise.repository.CalibrationMapRepository;
import com.zoneq.domain.seat.domain.Seat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoiseAttributionServiceTest {

    @Mock
    private CalibrationMapRepository calibrationMapRepository;

    @InjectMocks
    private NoiseAttributionService noiseAttributionService;

    @Test
    void attribute_returnsEmpty_whenNoMapsExist() {
        when(calibrationMapRepository.findByReceiverSeatId(1L)).thenReturn(List.of());

        Optional<Long> result = noiseAttributionService.attribute(1L, 50.0);

        assertThat(result).isEmpty();
    }

    @Test
    void attribute_returnsSeatWithHighestEstimatedSource() {
        // seatA: estimatedSource = 50.0 + 3.0 = 53.0
        // seatB: estimatedSource = 50.0 + 8.0 = 58.0 → 소음원
        Seat seatA = mock(Seat.class);
        Seat seatB = mock(Seat.class);
        when(seatB.getId()).thenReturn(2L);
        Seat receiver = mock(Seat.class);

        CalibrationMap mapA = CalibrationMap.of(seatA, receiver, 3.0);
        CalibrationMap mapB = CalibrationMap.of(seatB, receiver, 8.0);
        when(calibrationMapRepository.findByReceiverSeatId(99L)).thenReturn(List.of(mapA, mapB));

        Optional<Long> result = noiseAttributionService.attribute(99L, 50.0);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(2L); // seatB
    }

    @Test
    void attribute_returnsSingle_whenOnlyOneMapExists() {
        Seat sourceSeat = mock(Seat.class);
        when(sourceSeat.getId()).thenReturn(10L);
        Seat receiver = mock(Seat.class);

        CalibrationMap map = CalibrationMap.of(sourceSeat, receiver, 5.0);
        when(calibrationMapRepository.findByReceiverSeatId(99L)).thenReturn(List.of(map));

        Optional<Long> result = noiseAttributionService.attribute(99L, 50.0);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(10L);
    }
}
