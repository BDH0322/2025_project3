package com.example.ufc.Controller;
import com.example.ufc.DTO.MemberDTO;
import com.example.ufc.Entity.MemberEntity;
import com.example.ufc.Service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.example.ufc.DTO.MemberDTO;
import com.example.ufc.Service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MemberController {


    @Autowired
    MemberService memberService;


    @Autowired
    PasswordEncoder passwordEncoder;


    @GetMapping("/login")
    public String input(Model mo, MemberDTO memberDTO,HttpServletRequest request){
        // 파라미터에서 memberDTO를 제거하고, 명확하게 새로운 객체를 추가합니다.
        // 새로운 객체를 생성해주지 않으면 이전에 로그인 했던 dto 객체가 남아있을 수 있습니다.
        // 그래서 new MemberDTO로 새로운 객체를 생성하여 객체를 비워준다.
        mo.addAttribute("memberDTO", new MemberDTO());

        // 2. 이 경로를 'currentUri'라는 이름으로 Thymeleaf에 전달합니다.



        return "/login";
    }

    @PostMapping("/membersave")
    public String save(MemberDTO memberDTO, RedirectAttributes redirectAttributes) throws UnsupportedEncodingException {
        // 1. 비밀번호 불일치 검사
        if(!memberDTO.getPassword().equals(memberDTO.getPassword_confirm())) {
            String encodedMessage = URLEncoder.encode("비밀번호와 비밀번호 확인이 일치하지 않습니다.", StandardCharsets.UTF_8.toString());
            // 실패 시 DTO 정보를 유지하기 위해 FlashAttribute를 사용 (폼에 입력값 유지)
            redirectAttributes.addFlashAttribute("memberDTO", memberDTO);
            // URL 파라미터로 메시지 전달: ?status=fail&msg=...
            return "redirect:/login?status=fail&msg=" + encodedMessage;
        }

        boolean isSaved = memberService.save(memberDTO);

        if(isSaved){
            String encodedMessage = URLEncoder.encode("회원가입이 완료되었습니다. 로그인 해주세요!", StandardCharsets.UTF_8.toString());
            // 성공 시 URL 파라미터로 메시지 전달: ?status=success&msg=...
            return "redirect:/login?status=success&msg=" + encodedMessage;
        }
        else{
            String encodedMessage = URLEncoder.encode("이미 사용중인 아이디입니다. 다시 입력해주세요", StandardCharsets.UTF_8.toString());
            // 실패 시 DTO 정보를 유지하기 위해 FlashAttribute를 사용 (폼에 입력값 유지)
            redirectAttributes.addFlashAttribute("memberDTO", memberDTO);
            // URL 파라미터로 메시지 전달: ?status=fail&msg=...
            return "redirect:/login?status=fail&msg=" + encodedMessage;
        }

    }

    @GetMapping("/checkUserId")
    @ResponseBody
    //@RequestParam으로 받기 위해  URL-encoded POST body를 사용
    public String checkUserId(@RequestParam("id")String id){
        if(memberService.isUserIdDuplicated(id)){
            return "duplicate";
        } else{
            return "available";
        }

    }





}

