package com.example.straffic.member.serviceImp;

import com.example.straffic.member.dto.MemberCreateDTO;
import com.example.straffic.member.entity.MemberEntity;
import com.example.straffic.member.interfaces.MemberServiceInterface;
import com.example.straffic.member.repository.MemberRepository;
import com.example.straffic.member.service.MemberService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public void memberinsert(MemberCreateDTO memberDTO, HttpServletResponse response) {
        if (memberRepository.existsById(memberDTO.getId())) {
            throw new IllegalStateException("이미 가입된 아이디입니다");
        }

        MemberEntity me = new MemberEntity();
        me.setId(memberDTO.getId());
        me.setPw(bCryptPasswordEncoder.encode(memberDTO.getPw()));
        me.setName(memberDTO.getName());
        me.setTel(memberDTO.getTel());
        me.setRole("ROLE_USER");
        memberRepository.save(me);
    }

    @Override
    public List<MemberServiceInterface> memberOut() {
        return memberRepository.result();
    }

    @Override
    public List<MemberEntity> entityout() {
        return memberRepository.findAll();
    }

    @Override
    public Page<MemberEntity> entitypage(int page) {
        return memberRepository.findAll(PageRequest.of(page, 3));
    }

    @Override
    public long count() {
        return memberRepository.count();
    }
}
