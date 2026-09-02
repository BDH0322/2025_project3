package com.example.ufc.Service;

import com.example.ufc.Entity.MemberEntity;
import com.example.ufc.Repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailService implements UserDetailsService{

    private final MemberRepository memberRepository;

    /**
     * Spring Security의 UserDetailsService 인터페이스의 핵심 메서드입니다.
     * 사용자명(ID)을 기반으로 데이터베이스에서 사용자 정보를 로드하여 UserDetails 객체로 반환합니다.
     *
     * @param username 로그인 시 입력된 사용자 ID (여기서는 MemberEntity의 id)
     * @return UserDetails 객체 (CustomUserDetails 구현체가 되어야 함)
     * @throws UsernameNotFoundException 해당 ID의 사용자가 없을 경우 발생
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 1. Repository를 사용하여 사용자 ID(username)로 MemberEntity를 조회합니다.
        // Optional을 사용하지 않고 orElseThrow를 사용하여 예외를 명확히 처리합니다.

       MemberEntity member = memberRepository.findById(username)
               .orElseThrow(() -> new UsernameNotFoundException("해당 사용자를 찾을 수 없습니다: " + username));



        return new CustomUserDetails(member);
    }
}
