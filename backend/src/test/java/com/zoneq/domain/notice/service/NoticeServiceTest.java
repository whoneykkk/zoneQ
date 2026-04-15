package com.zoneq.domain.notice.service;

import com.zoneq.domain.notice.domain.Notice;
import com.zoneq.domain.notice.dto.*;
import com.zoneq.domain.notice.repository.NoticeRepository;
import com.zoneq.domain.user.domain.User;
import com.zoneq.domain.user.domain.UserRole;
import com.zoneq.domain.user.repository.UserRepository;
import com.zoneq.global.exception.BusinessException;
import com.zoneq.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {

    @Mock private NoticeRepository noticeRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private NoticeService noticeService;

    private User mockAdmin;

    @BeforeEach
    void setUp() {
        mockAdmin = User.create("관리자", "admin@test.com", "pw", UserRole.ADMIN);
    }

    @Test
    void getList_withPinnedTrue_callsFindAllPinned() {
        PageRequest unsorted = PageRequest.of(0, 10);
        when(noticeRepository.findAllPinned(unsorted)).thenReturn(Page.empty());

        NoticeListResponse result = noticeService.getList(true, PageRequest.of(0, 10));

        verify(noticeRepository).findAllPinned(unsorted);
        verify(noticeRepository, never()).findAllSorted(any());
        assertThat(result.notices()).isEmpty();
    }

    @Test
    void getList_withPinnedNull_callsFindAllSorted() {
        PageRequest unsorted = PageRequest.of(0, 10);
        when(noticeRepository.findAllSorted(unsorted)).thenReturn(Page.empty());

        noticeService.getList(null, PageRequest.of(0, 10));

        verify(noticeRepository).findAllSorted(unsorted);
        verify(noticeRepository, never()).findAllPinned(any());
    }

    @Test
    void getDetail_returnsResponse_whenFound() {
        Notice notice = Notice.create(mockAdmin, "제목", "본문", false);
        when(noticeRepository.findById(1L)).thenReturn(Optional.of(notice));

        NoticeResponse result = noticeService.getDetail(1L);

        assertThat(result.title()).isEqualTo("제목");
        assertThat(result.adminName()).isEqualTo("관리자");
    }

    @Test
    void getDetail_throwsNotFound_whenMissing() {
        when(noticeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noticeService.getDetail(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.NOTICE_NOT_FOUND.getMessage());
    }

    @Test
    void create_savesAndReturnsResponse() {
        NoticeCreateRequest req = new NoticeCreateRequest("새 공지", "내용", true);
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(mockAdmin));
        when(noticeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        NoticeResponse result = noticeService.create("admin@test.com", req);

        verify(noticeRepository).save(any(Notice.class));
        assertThat(result.title()).isEqualTo("새 공지");
        assertThat(result.isPinned()).isTrue();
    }

    @Test
    void update_changesOnlyNonNullFields() {
        Notice notice = Notice.create(mockAdmin, "원제목", "원본문", false);
        when(noticeRepository.findById(1L)).thenReturn(Optional.of(notice));

        NoticeUpdateRequest req = new NoticeUpdateRequest("수정제목", null, true);
        NoticeResponse result = noticeService.update(1L, req);

        assertThat(result.title()).isEqualTo("수정제목");
        assertThat(result.body()).isEqualTo("원본문");
        assertThat(result.isPinned()).isTrue();
    }

    @Test
    void update_throwsNotFound_whenMissing() {
        when(noticeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noticeService.update(999L, new NoticeUpdateRequest(null, null, null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.NOTICE_NOT_FOUND.getMessage());
    }

    @Test
    void delete_callsRepositoryDelete() {
        Notice notice = Notice.create(mockAdmin, "제목", "본문", false);
        when(noticeRepository.findById(1L)).thenReturn(Optional.of(notice));

        noticeService.delete(1L);

        verify(noticeRepository).delete(notice);
    }

    @Test
    void delete_throwsNotFound_whenMissing() {
        when(noticeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noticeService.delete(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.NOTICE_NOT_FOUND.getMessage());
    }

    @Test
    void getList_withPinnedFalse_callsFindAllSorted() {
        PageRequest unsorted = PageRequest.of(0, 10);
        when(noticeRepository.findAllSorted(unsorted)).thenReturn(Page.empty());

        noticeService.getList(false, PageRequest.of(0, 10));

        verify(noticeRepository).findAllSorted(unsorted);
        verify(noticeRepository, never()).findAllPinned(any());
    }

    @Test
    void create_throwsUserNotFound_whenAdminMissing() {
        when(userRepository.findByEmail("no@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noticeService.create("no@test.com",
                new NoticeCreateRequest("제목", "본문", false)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
    }
}
