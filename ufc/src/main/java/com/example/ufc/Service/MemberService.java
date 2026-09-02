package com.example.ufc.Service;

import com.example.ufc.DTO.MemberDTO;
import com.example.ufc.Entity.MemberEntity;


public interface MemberService {

    void insert(MemberEntity member);

    boolean isUserIdExists(String id);

    boolean save(MemberDTO memberDTO);

    boolean isUserIdDuplicated(String id);
}
