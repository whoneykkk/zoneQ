package com.zoneq.domain.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record RealtimeSeatData(
        @Schema(description = "좌석 ID", example = "1") Long seatId,
        @Schema(description = "구역", example = "A") String zone,
        @Schema(description = "좌석 번호", example = "3") int seatNumber,
        @Schema(description = "최근 leqDb (측정값 없으면 null)", example = "52.3") Double leqDb,
        @Schema(description = "착석 이용자 ID", example = "7") Long userId
) {}
