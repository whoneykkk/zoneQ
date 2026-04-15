package com.zoneq.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),

    // Auth
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    ACCOUNT_LOCKED(HttpStatus.UNAUTHORIZED, "로그인 5회 실패로 계정이 잠겼습니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EMAIL_DUPLICATED(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),

    // Seat
    SEAT_NOT_FOUND(HttpStatus.NOT_FOUND, "좌석을 찾을 수 없습니다."),
    SEAT_ALREADY_OCCUPIED(HttpStatus.CONFLICT, "이미 사용 중인 좌석입니다."),
    NO_SEAT_AVAILABLE(HttpStatus.NOT_FOUND, "배정 가능한 좌석이 없습니다."),
    ALREADY_HAS_SEAT(HttpStatus.CONFLICT, "이미 좌석이 배정되어 있습니다."),

    // Session
    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "활성 세션을 찾을 수 없습니다."),

    // Noise
    NOISE_MEASUREMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "소음 측정 데이터를 찾을 수 없습니다."),

    // Grade
    GRADE_NOT_FOUND(HttpStatus.NOT_FOUND, "등급 정보를 찾을 수 없습니다."),

    // Message
    MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "쪽지를 찾을 수 없습니다."),
    REPLY_NOT_ALLOWED(HttpStatus.FORBIDDEN, "익명 쪽지에는 답장할 수 없습니다."),

    // Notice
    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "공지사항을 찾을 수 없습니다."),

    // Notification
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
