package com.example.ufc.DTO;


import com.example.ufc.Entity.MemberEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MemberDTO {
    String id;
    String password;
    String email;
    String name;
    String birth;
    String phone;
    int admin = 0;
    String password_confirm;
    private LocalDateTime banEndData;
    public MemberEntity toEntity(){return new MemberEntity(id,password,email,name,birth,phone,admin,banEndData);}


}
