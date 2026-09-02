package com.example.ufc.Controller;

import com.example.ufc.DTO.CommunityReplyDTO;
import com.example.ufc.Entity.CommunityReplyEntity;
import com.example.ufc.Entity.MemberEntity;
import com.example.ufc.Repository.MemberRepository;
import com.example.ufc.Service.CommunityReplyService;
import com.example.ufc.Service.CommunityReplyVoteService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping(value = "/community")
public class CommunityReplyController {

    @Autowired
    CommunityReplyService communityReplyService; // crud는 여기서

    @Autowired
    CommunityReplyVoteService communityReplyVoteService; // 투표는 여기서

    @Autowired
    MemberRepository memberRepository;

    @PostMapping(value = "/replysave")
    public String replySave(CommunityReplyDTO communityReplyDTO, Model model){
        CommunityReplyEntity communityReplyEntity = communityReplyDTO.entity();
        communityReplyService.replysave(communityReplyEntity);

        return "redirect:/community/communitypost?communityContentNumber=" + communityReplyDTO.getCommunityContentNumber();
    }

    @PostMapping(value = "/replyreplysave")
    public String replyReplySave(CommunityReplyDTO communityReplyDTO){
        CommunityReplyEntity replyreplyentity = communityReplyDTO.entity();
        communityReplyService.replyreplysave(replyreplyentity);

        return "redirect:/community/communitypost?communityContentNumber=" + communityReplyDTO.getCommunityContentNumber();
    }

    @PostMapping(value = "/replydelete")
    public String replyDelete(@RequestParam("replyNumber") Long replyNumber,
                                  @RequestParam("communityContentNumber") Long communityContentNumber, CommunityReplyDTO communityReplyDTO){
        communityReplyService.replydelete(replyNumber);

        return "redirect:/community/communitypost?communityContentNumber=" + communityContentNumber;
    }

    @PostMapping(value = "/replylike")
    @ResponseBody
    public String replyLike(@RequestParam Long replyNumber, @RequestParam String id){
        communityReplyVoteService.replylike(replyNumber, id);

        return "success";
    }

    @PostMapping(value = "replydislike")
    @ResponseBody
    public String replydislike(@RequestParam Long replyNumber, @RequestParam String id){
        communityReplyVoteService.replydislike(replyNumber, id);

        return "success";
    }

    @GetMapping(value = "replyvote")
    @ResponseBody
    public int replyvote(@RequestParam Long replyNumber, @RequestParam String id){

        return communityReplyVoteService.vote(replyNumber, id);
    }

    @PostMapping(value = "/hidereply") ///community/hidereply
    public String adminHideReply(@RequestParam Long replyNumber, @RequestParam Long communityContentNumber, HttpSession session){
        String id = (String) session.getAttribute("id");
        MemberEntity member = memberRepository.findById(id).orElse(null);

        if (member == null || member.getAdmin() != 1){
            return "redirect:/login";
        }
        communityReplyService.hideReply(replyNumber);

        return "redirect:/community/communitypost?communityContentNumber=" + communityContentNumber;
    }

    @PostMapping(value = "/unhidereply") ///community/unhidereply
    public String adminUnhideReply(@RequestParam Long replyNumber, @RequestParam Long communityContentNumber, HttpSession session){
        String id = (String) session.getAttribute("id");
        MemberEntity member = memberRepository.findById(id).orElse(null);

        if (member == null || member.getAdmin() != 1){
            return "redirect:/login";
        }
        communityReplyService.unhideReply(replyNumber);

        return "redirect:/community/communitypost?communityContentNumber=" + communityContentNumber;
    }

    @PostMapping(value = "/deletereply") ///community/deletereply
    public String adminDeleteReply(@RequestParam Long replyNumber, @RequestParam Long communityContentNumber, HttpSession session){
        String id = (String) session.getAttribute("id");
        MemberEntity member = memberRepository.findById(id).orElse(null);

        if (member == null || member.getAdmin() != 1){
            return "redirect:/login";
        }
        communityReplyService.deleteReplyHard(replyNumber);

        return "redirect:/community/communitypost?communityContentNumber=" + communityContentNumber;
    }

}
