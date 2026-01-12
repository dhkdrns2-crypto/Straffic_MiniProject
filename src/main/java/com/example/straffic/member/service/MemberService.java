package com.example.straffic.member.service;

import com.example.straffic.member.dto.MemberCreateDTO;
import com.example.straffic.member.entity.MemberEntity;
import com.example.straffic.member.interfaces.MemberServiceInterface;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MemberService {
    void memberinsert(MemberCreateDTO memberDTO, HttpServletResponse response);

    List<MemberServiceInterface> memberOut();

    List<MemberEntity> entityout();

    Page<MemberEntity> entitypage(int page);

    long count();
}
