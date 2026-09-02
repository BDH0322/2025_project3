package com.example.ufc.Service;

import com.example.ufc.DTO.MemberDTO;
import com.example.ufc.Entity.MemberEntity;
import com.example.ufc.Repository.MemberRepository;
import io.micrometer.observation.annotation.Observed;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

// 이 생성자가 주입을 담당합니다 충돌가능성이 있으므로 autowerid 뻄
@AllArgsConstructor
@Service
public class MemberServiceImpl implements MemberService {


    MemberRepository memberRepository;

    BCryptPasswordEncoder bCryptPasswordEncoder;


    public void insert(MemberEntity member)
    {memberRepository.save(member);}


   @Override
    public boolean isUserIdExists(String id) {
        return memberRepository.existsById(id);
    }


    @Override
    public boolean save(MemberDTO memberDTO){
        if(isUserIdDuplicated(memberDTO.getId())){
            return false;
        }

        String encodePassword = bCryptPasswordEncoder.encode(memberDTO.getPassword());
        memberDTO.setPassword(encodePassword);

        MemberEntity memberEntity = memberDTO.toEntity();
        memberRepository.save(memberEntity);
        return true;
    }

    @Override
    public boolean isUserIdDuplicated(String id){
        return memberRepository.existsById(id);
    }
}
