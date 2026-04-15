package com.zoneq.domain.notice.service;

import com.zoneq.domain.notice.domain.Notice;
import com.zoneq.domain.notice.dto.*;
import com.zoneq.domain.notice.repository.NoticeRepository;
import com.zoneq.domain.user.domain.User;
import com.zoneq.domain.user.repository.UserRepository;
import com.zoneq.global.exception.BusinessException;
import com.zoneq.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public NoticeListResponse getList(Boolean pinned, Pageable pageable) {
        Pageable unsorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        Page<Notice> page = Boolean.TRUE.equals(pinned)
                ? noticeRepository.findAllPinned(unsorted)
                : noticeRepository.findAllSorted(unsorted);
        return NoticeListResponse.from(page.map(NoticeResponse::from));
    }

    @Transactional(readOnly = true)
    public NoticeResponse getDetail(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));
        return NoticeResponse.from(notice);
    }

    @Transactional
    public NoticeResponse create(String adminEmail, NoticeCreateRequest request) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Notice notice = Notice.create(admin, request.title(), request.body(), request.isPinned());
        return NoticeResponse.from(noticeRepository.save(notice));
    }

    @Transactional
    public NoticeResponse update(Long id, NoticeUpdateRequest request) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));
        notice.update(request.title(), request.body(), request.isPinned());
        return NoticeResponse.from(notice);
    }

    @Transactional
    public void delete(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));
        noticeRepository.delete(notice);
    }
}
