package com.example.straffic.notice.repository;

import com.example.straffic.notice.entity.NoticeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NoticeRepository extends JpaRepository<NoticeEntity, Long> {
    @Query("""
            select n from NoticeEntity n
            where (:q is null or :q = '' or n.title like concat('%', :q, '%'))
              and (:tag is null or :tag = '' or n.tag = :tag)
            order by n.pinned desc, n.createdAt desc
            """)
    Page<NoticeEntity> search(String q, String tag, Pageable pageable);
}

