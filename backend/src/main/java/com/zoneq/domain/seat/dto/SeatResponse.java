package com.zoneq.domain.seat.dto;

import com.zoneq.domain.seat.domain.Seat;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "좌석 응답")
public record SeatResponse(
        @Schema(description = "좌석 ID", example = "1")
        Long id,

        @Schema(description = "구역", example = "S")
        String zone,

        @Schema(description = "좌석 번호", example = "1")
        int seatNumber,

        @Schema(description = "상태", example = "AVAILABLE")
        String status,

        @Schema(description = "착석 중인 이용자 ID (없으면 null)")
        Long userId
) {
    public static SeatResponse from(Seat seat) {
        return new SeatResponse(
                seat.getId(),
                seat.getZone(),
                seat.getSeatNumber(),
                seat.getStatus().name(),
                seat.getUser() != null ? seat.getUser().getId() : null
        );
    }
}
