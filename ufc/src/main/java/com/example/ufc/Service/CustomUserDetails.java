package com.example.ufc.Service;
import com.example.ufc.Entity.MemberEntity;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;


@AllArgsConstructor

public class CustomUserDetails implements UserDetails {

    private final MemberEntity member;
/*
    // 3. 권한 목록을 반환합니다. (사용자 역할(Role)을 기반으로 권한을 설정해야 합니다.)
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
        // 모든 일반 회원은 ROLE_USER 권한을 가진다고 가정합니다.
        // 만약 MemberEntity에 역할(role) 필드가 있다면 그 필드를 사용해야 합니다.
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }
  */

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        //admin필드가 1이먄 ROLE_ADMIN 0이면 ROLE_USER 권한부여
        if(member.getAdmin() == 1){
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else{
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }
        return authorities;
    }

    // 4. 암호화된 비밀번호를 반환합니다.
    @Override
    public String getPassword(){
        return member.getPassword();
    }

    public String getId(){return member.getId();}
    @Override
    public String getUsername(){
        return member.getName();
    }

    @Override
    public boolean isAccountNonExpired(){
        return true;
    }
    @Override
    public boolean isAccountNonLocked(){
        return true;
    }

    @Override
    public boolean isEnabled(){
        return true;
    }

    public MemberEntity getMemberEntity(){
        return member;
    }

    public int getAdmin(){return member.getAdmin();}

}
