package com.zoneq.domain.notice.repository;

import com.zoneq.domain.notice.domain.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    @Query("SELECT n FROM Notice n WHERE n.pinned = true ORDER BY n.createdAt DESC")
    Page<Notice> findAllPinned(Pageable pageable);

    @Query("SELECT n FROM Notice n ORDER BY n.pinned DESC, n.createdAt DESC")
    Page<Notice> findAllSorted(Pageable pageable);
}
