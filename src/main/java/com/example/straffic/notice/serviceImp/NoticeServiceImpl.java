package com.example.straffic.notice.serviceImp;

import com.example.straffic.member.entity.MemberEntity;
import com.example.straffic.member.repository.MemberRepository;
import com.example.straffic.notice.entity.NoticeEntity;
import com.example.straffic.notice.repository.NoticeRepository;
import com.example.straffic.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {
    private final NoticeRepository noticeRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public NoticeEntity create(String title, String content, String tag, boolean pinned, String authorId, MultipartFile image) {
        NoticeEntity n = new NoticeEntity();
        n.setTitle(title);
        n.setContent(content);
        n.setTag(tag);
        n.setPinned(pinned);

        if (authorId != null) {
            MemberEntity author = memberRepository.findOneById(authorId);
            n.setAuthor(author);
        }

        if (image != null && !image.isEmpty()) {
            n.setImageOriginalName(image.getOriginalFilename());
            n.setImageContentType(image.getContentType() != null ? image.getContentType() : "application/octet-stream");
            try {
                n.setImageData(image.getBytes());
            } catch (IOException e) {
                throw new IllegalStateException("이미지 업로드 중 오류가 발생했습니다");
            }
        }

        return noticeRepository.save(n);
    }

    @Override
    @Transactional
    public NoticeEntity update(Long id, String title, String content, String tag, boolean pinned, MultipartFile image) {
        NoticeEntity n = noticeRepository.findById(id).orElseThrow(() -> new IllegalStateException("공지사항을 찾을 수 없습니다"));
        n.setTitle(title);
        n.setContent(content);
        n.setTag(tag);
        n.setPinned(pinned);

        if (image != null && !image.isEmpty()) {
            n.setImageOriginalName(image.getOriginalFilename());
            n.setImageContentType(image.getContentType() != null ? image.getContentType() : "application/octet-stream");
            try {
                n.setImageData(image.getBytes());
            } catch (IOException e) {
                throw new IllegalStateException("이미지 업로드 중 오류가 발생했습니다");
            }
        }

        return noticeRepository.save(n);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        NoticeEntity n = noticeRepository.findById(id).orElseThrow(() -> new IllegalStateException("공지사항을 찾을 수 없습니다"));
        noticeRepository.delete(n);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoticeEntity> list(int page, String q, String tag) {
        try {
            return noticeRepository.search(q, tag, PageRequest.of(page, 10));
        } catch (RuntimeException e) {
            return new PageImpl<>(Collections.emptyList(), PageRequest.of(page, 10), 0);
        }
    }

    @Override
    @Transactional
    public NoticeEntity findAndIncreaseViews(Long id) {
        NoticeEntity n = noticeRepository.findById(id).orElseThrow(() -> new IllegalStateException("공지사항을 찾을 수 없습니다"));
        n.setViews(n.getViews() + 1);
        return noticeRepository.save(n);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getImageData(Long noticeId) {
        NoticeEntity n = noticeRepository.findById(noticeId).orElseThrow(() -> new IllegalStateException("공지사항을 찾을 수 없습니다"));
        return n.getImageData();
    }

    @Override
    @Transactional(readOnly = true)
    public NoticeEntity get(Long id) {
        return noticeRepository.findById(id).orElseThrow(() -> new IllegalStateException("공지사항을 찾을 수 없습니다"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoticeEntity> getRecentNotices() {
        try {
            return noticeRepository.findTop3ByOrderByCreatedAtDesc();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
