package com.example.ufc.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Setter
@Getter
@Table(name="ufcggmember")
public class MemberEntity {
    @Id
    @Column
    String id;
    @Column// 아이디 (id)
    String password;  // 비밀번호 (pwd)
    // private String pwdConfirm; // 비밀번호 확인 필드는 DTO에 보통 포함하지 않음
    @Column
    String email;     // 이메일
    @Column
    String name;      // 이름
    @Column
    String birth;     // 생년월일 (String 타입으로 받고, 필요시 Date로 변환)
    @Column
    String phone;     // 전화번호
    @Column
    int admin;
    // 추가할 필드
    @Column(name = "BAN_END_DATE")
    private LocalDateTime banEndDate;


}
