package com.example.straffic.notice.service;

import com.example.straffic.notice.entity.NoticeEntity;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface NoticeService {
    NoticeEntity create(String title, String content, String tag, boolean pinned, String authorId, MultipartFile image);
    NoticeEntity update(Long id, String title, String content, String tag, boolean pinned, MultipartFile image);
    void delete(Long id);
    Page<NoticeEntity> list(int page, String q, String tag);
    NoticeEntity findAndIncreaseViews(Long id);
    byte[] getImageData(Long noticeId);
    NoticeEntity get(Long id);
}
