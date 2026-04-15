package com.zoneq.domain.noise.controller;

import com.zoneq.domain.noise.dto.*;
import com.zoneq.domain.noise.service.NoiseService;
import com.zoneq.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Noise", description = "소음 측정 API")
@RestController
@RequestMapping("/api/noise")
@RequiredArgsConstructor
public class NoiseController {

    private final NoiseService noiseService;

    @Operation(summary = "소음 측정값 전송",
               description = "Web Audio API에서 30초 단위로 측정한 Leq값 전송. 비동기 처리로 즉시 202 반환.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "수신 완료 (비동기 처리)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/measurements")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ApiResponse<Void> saveMeasurement(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody NoiseMeasurementRequest request) {
        noiseService.saveMeasurement(userDetails.getUsername(), request);
        return ApiResponse.ok();
    }

    @Operation(summary = "캘리브레이션 실행 (ADMIN)",
               description = "좌석별 감쇠 맵 생성. 기존 맵 전체 삭제 후 새 맵으로 교체.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "캘리브레이션 완료, 생성된 맵 개수 반환"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "entries 비어있음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ADMIN 권한 필요")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/calibration")
    public ApiResponse<Integer> saveCalibration(
            @Valid @RequestBody CalibrationRequest request) {
        return ApiResponse.ok(noiseService.saveCalibration(request));
    }

    @Operation(summary = "소음 분류 결과 조회",
               description = "측정 ID로 소음 유형(TALK/KEYBOARD/COUGH/OTHER)과 습관성 여부 조회.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "측정 데이터 없음")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/classifications/{id}")
    public ApiResponse<NoiseClassificationResponse> getClassification(
            @Parameter(description = "noise_measurement ID") @PathVariable Long id) {
        return ApiResponse.ok(noiseService.getClassification(id));
    }
}
