package com.example.straffic.security;

import com.example.straffic.member.entity.MemberEntity;
import com.example.straffic.member.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final MemberRepository memberRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String id) {
        log.info("현재 로그인 중 id : " + id);
        MemberEntity memberEntity = memberRepository.findOneById(id);
        if (memberEntity == null) {
            throw new UsernameNotFoundException("User not found");
        }

        String role = memberEntity.getRole();
        
        if ("admin".equals(memberEntity.getId()) && "어드민".equals(memberEntity.getName())) {
            role = "ROLE_ADMIN";
        }
        
        if (role == null || role.trim().isEmpty()) {
            role = "ROLE_USER";
        }
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }

        return User.builder()
                .username(memberEntity.getId())
                .password(memberEntity.getPw())
                .authorities(role)
                .build();
    }
}
