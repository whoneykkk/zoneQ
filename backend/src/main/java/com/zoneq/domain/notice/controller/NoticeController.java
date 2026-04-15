package com.zoneq.domain.notice.controller;

import com.zoneq.domain.notice.dto.*;
import com.zoneq.domain.notice.service.NoticeService;
import com.zoneq.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Notice", description = "공지사항 API")
@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @Operation(summary = "공지사항 목록 조회",
               description = "pinned=true면 고정 공지만 반환. page/size로 페이지네이션.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ApiResponse<NoticeListResponse> getList(
            @Parameter(description = "true면 고정 공지만 조회") @RequestParam(required = false) Boolean pinned,
            @Parameter(description = "페이지 번호 (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(noticeService.getList(pinned, PageRequest.of(page, size)));
    }

    @Operation(summary = "공지사항 상세 조회")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공지사항 없음")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}")
    public ApiResponse<NoticeResponse> getDetail(
            @Parameter(description = "공지사항 ID") @PathVariable Long id) {
        return ApiResponse.ok(noticeService.getDetail(id));
    }

    @Operation(summary = "공지사항 등록 (ADMIN)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ADMIN 권한 필요")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<NoticeResponse> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody NoticeCreateRequest request) {
        return ApiResponse.ok(noticeService.create(userDetails.getUsername(), request));
    }

    @Operation(summary = "공지사항 수정 (ADMIN)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공지사항 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ADMIN 권한 필요")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{id}")
    public ApiResponse<NoticeResponse> update(
            @Parameter(description = "공지사항 ID") @PathVariable Long id,
            @RequestBody NoticeUpdateRequest request) {
        return ApiResponse.ok(noticeService.update(id, request));
    }

    @Operation(summary = "공지사항 삭제 (ADMIN)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공지사항 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ADMIN 권한 필요")
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(description = "공지사항 ID") @PathVariable Long id) {
        noticeService.delete(id);
    }
}
