package com.zoneq.domain.seat.controller;

import com.zoneq.domain.seat.dto.SeatResponse;
import com.zoneq.domain.seat.service.SeatService;
import com.zoneq.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Seat", description = "좌석 API")
@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @Operation(summary = "전체 좌석 현황 조회", description = "zone 파라미터로 구역 필터링 가능 (S|A|B|C)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ApiResponse<List<SeatResponse>> getSeats(
            @Parameter(description = "구역 필터 (S, A, B, C)")
            @RequestParam(required = false) String zone
    ) {
        return ApiResponse.ok(seatService.getSeats(zone));
    }
}
