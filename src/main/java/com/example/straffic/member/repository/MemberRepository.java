package com.example.straffic.member.repository;

import com.example.straffic.member.entity.MemberEntity;
import com.example.straffic.member.interfaces.MemberServiceInterface;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, String> {
    boolean existsById(String id);

    MemberEntity findOneById(String id);

    @Query(value = """
               select id, name, tel 
              from mem1213 """, nativeQuery = true)
    List<MemberServiceInterface> result();

    @Query(value = """
select * 
from mem1213
""", nativeQuery = true)
    Page<MemberServiceInterface> interpage(Pageable pageable);
}
