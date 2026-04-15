package com.zoneq.domain.message.controller;

import com.zoneq.domain.message.dto.*;
import com.zoneq.domain.message.service.MessageService;
import com.zoneq.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Message", description = "쪽지 API")
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @Operation(summary = "쪽지 발송", description = "receiverSeatId로 수신자를 지정. 발신자는 착석 중이어야 함")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "발송 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "좌석 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ApiResponse<MessageSendResponse> send(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MessageSendRequest request) {
        return ApiResponse.ok(messageService.send(userDetails.getUsername(), request));
    }

    @Operation(summary = "받은 쪽지 목록", description = "익명 발신은 senderSeat='익명' 고정")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/inbox")
    public ApiResponse<List<MessageInboxResponse>> getInbox(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ApiResponse.ok(messageService.getInbox(userDetails.getUsername()));
    }

    @Operation(summary = "쪽지 상세 조회", description = "본인 수신 쪽지만 조회 가능. 조회 시 읽음 처리")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "수신자 불일치"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "쪽지 없음")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}")
    public ApiResponse<MessageDetailResponse> getMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "쪽지 ID") @PathVariable Long id) {
        return ApiResponse.ok(messageService.getMessage(userDetails.getUsername(), id));
    }

    @Operation(summary = "쪽지 답장", description = "익명 쪽지에 답장 시 403 REPLY_NOT_ALLOWED")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "답장 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "익명 쪽지 답장 불가")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{id}/reply")
    public ApiResponse<MessageSendResponse> reply(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "원본 쪽지 ID") @PathVariable Long id,
            @Valid @RequestBody MessageReplyRequest request) {
        return ApiResponse.ok(messageService.reply(userDetails.getUsername(), id, request));
    }
}
