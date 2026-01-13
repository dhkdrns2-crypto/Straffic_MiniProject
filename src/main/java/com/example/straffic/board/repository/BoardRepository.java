package com.example.straffic.board.repository;

import com.example.straffic.board.entity.BoardEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<BoardEntity, Long> {
    List<BoardEntity> findTop3ByOrderByViewsDesc();
    List<BoardEntity> findByPinnedTrueOrderByCreatedAtDesc();

    @Query("""
            select b from BoardEntity b
            where (:q is null or :q = '' 
                   or (:type = 'title' and b.title like concat('%', :q, '%'))
                   or (:type = 'author' and b.author.name like concat('%', :q, '%'))
                   or (:type is null and (b.title like concat('%', :q, '%') or b.author.name like concat('%', :q, '%')))
                  )
              and b.pinned = false
              and (:excludeIds is null or b.id not in :excludeIds)
            order by b.createdAt desc
            """)
    Page<BoardEntity> search(@Param("q") String q, @Param("type") String type, @Param("excludeIds") List<Long> excludeIds, Pageable pageable);
}

