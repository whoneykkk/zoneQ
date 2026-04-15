package com.zoneq.domain.grade.controller;

import com.zoneq.domain.grade.dto.*;
import com.zoneq.domain.grade.service.GradeService;
import com.zoneq.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Grade", description = "소음 등급 조회 API")
@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    @Operation(summary = "내 등급 조회", description = "최근 5회 방문 기반 등급 및 점수 구성 반환")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ApiResponse<GradeScoreResponse> getMyGrade(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ApiResponse.ok(gradeService.getMyGrade(userDetails.getUsername()));
    }

    @Operation(summary = "내 등급 변화 이력", description = "등급이 변경된 기록을 최신순으로 반환")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/history/me")
    public ApiResponse<List<GradeHistoryResponse>> getMyHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ApiResponse.ok(gradeService.getMyHistory(userDetails.getUsername()));
    }

    @Operation(summary = "특정 이용자 등급 조회 (ADMIN)", description = "userId로 특정 이용자의 등급 및 점수 구성 조회")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "이용자 없음")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/users/{userId}")
    public ApiResponse<GradeDetailResponse> getUserGrade(
            @Parameter(description = "조회할 이용자 ID") @PathVariable Long userId) {
        return ApiResponse.ok(gradeService.getUserGrade(userId));
    }

    @Operation(summary = "전체 등급 분포 (ADMIN)", description = "S/A/B/C/미부여 이용자 수 통계 반환")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/distribution")
    public ApiResponse<GradeDistributionResponse> getDistribution() {
        return ApiResponse.ok(gradeService.getDistribution());
    }
}
