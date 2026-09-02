package com.example.ufc.Controller;

import com.example.ufc.DTO.CommunityDTO;
import com.example.ufc.DTO.CommunityReplyDTO;
import com.example.ufc.Entity.CommunityEntity;
import com.example.ufc.Entity.MemberEntity;
import com.example.ufc.Repository.MemberRepository;
import com.example.ufc.Service.CommunityReplyService;
import com.example.ufc.Service.CommunityReplyVoteService;
import com.example.ufc.Service.CommunityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Controller
public class CommunityController {

    @Autowired
    CommunityService communityService;

    @Autowired
    CommunityReplyService communityReplyService;

    @Autowired // 댓글 투표 상태 가져오기
    CommunityReplyVoteService communityReplyVoteService;

    @Autowired
    MemberRepository memberRepository;

    @GetMapping(value = {"/community", "/community/communitysearch"})
    public String communityList(
            HttpServletRequest request,
            HttpSession session,
            Model model,
            @RequestParam(value = "category", required = false) Integer category,
            @RequestParam(value = "searchType", defaultValue = "communityTitle") String searchType,
            @RequestParam(value = "keyword", defaultValue = "") String keyword,
            @PageableDefault(size = 10, page = 0) Pageable pageable) {

        model.addAttribute("currentUri", request.getRequestURI());
        String id = (String) session.getAttribute("id");

        MemberEntity member = memberRepository.findById(id).orElse(null);
        int admin = (member != null) ? member.getAdmin() : 0;
        boolean banned = member != null && member.getBanEndDate() != null && member.getBanEndDate().isAfter(LocalDateTime.now());

        model.addAttribute("banned", banned);
        model.addAttribute("admin", admin);

        Sort sort = Sort.by(
                Sort.Order.desc("communityIsNotice"),
                Sort.Order.asc("communityWriteTime")
        );
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        Page<CommunityEntity> list;

        // 검색 기능
        if (!keyword.isEmpty()) {
            if (category != null) {
                list = communityService.getCommunityListByCategoryAndSearch(category, searchType, keyword, pageable);
            } else {
                list = communityService.CommunitySearch(searchType, keyword, pageable);
            }
        } else if (category != null) {
            list = communityService.getCommunityListByCategory(category, pageable);
        } else {
            list = communityService.findcommunity(pageable);
        }

        model.addAttribute("list", list);

        int currentPage = list.getPageable().getPageNumber();
        int startPage = Math.max(0, currentPage / 10 * 10);
        int endPage = Math.min(list.getTotalPages() == 0 ? 0 : list.getTotalPages() - 1, startPage + 9);

        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("searchType", searchType);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentCategory", category);

        return "community/community";
    }


    @GetMapping(value = "/community/communitywrite")
    public String CommunityWrite(HttpServletRequest request, Model model, CommunityDTO communityDTO, HttpSession session) {
        model.addAttribute("currentUri", request.getRequestURI());

        String id = (String) session.getAttribute("id");
        MemberEntity member = memberRepository.findById(id).orElse(null);
        int admin = (member != null) ? member.getAdmin() : 0;

        model.addAttribute("id", id);
        model.addAttribute("admin", admin);

        return "community/communitywrite";
    }

    @PostMapping(value = "/community/communityWriteSave")
    public String CommunityWriteSave(CommunityDTO communityDTO, @RequestParam("communityimage") MultipartFile communityimage, HttpSession session) throws IOException {
        String id = (String) session.getAttribute("id");
        MemberEntity member = memberRepository.findById(id).orElse(null);
        communityDTO.setAdmin(member.getAdmin());

        if(member.getBanEndDate() != null && member.getBanEndDate().isAfter(LocalDateTime.now())){
            return "redirect:/community?error=banned";
        }

        if (communityDTO.getCommunityCategory() == 8000) {
            if (member.getAdmin() != 1) {
                return "redirect:/community?error=notadmin";
            }
            communityDTO.setCommunityIsNotice(1); // 공지글 표시
        } else {
            communityDTO.setCommunityIsNotice(0); // 일반글
        }

        if(communityimage != null && !communityimage.isEmpty()) {
            String imagename = communityimage.getOriginalFilename(); //유저가 올린 사진을 받아와서 그 이름을 객체에 저장
            String imagefile = UUID.randomUUID().toString() + "_" + imagename; //객체에 저장된 이름에 파일처리
            File saveFile = new File("C:\\project\\Data\\community\\image\\" + imagefile);
            communityimage.transferTo(saveFile);
            communityDTO.setCommunityImage(imagefile);
        } else {
            communityDTO.setCommunityImage(null);
        }
        CommunityEntity centity = communityDTO.entity();
        communityService.contentinsert(centity);

        return "redirect:/community";
    }

    @GetMapping(value = "/community/communitypost") //여기서 키 값 받고 조회해서 띄워줌
    public String CommunityPost(@RequestParam("communityContentNumber") Long communityContentNumber, HttpServletRequest request, Model model, HttpSession session) {
        model.addAttribute("currentUri", request.getRequestURI());

        String id = (String) session.getAttribute("id");
        model.addAttribute("id", id);

        // admin 권한 확인
        MemberEntity member = memberRepository.findById(id).orElse(null);
        int admin = (member != null) ? member.getAdmin() : 0;
        model.addAttribute("admin", admin);

        // 게시글 조회
        CommunityEntity post = communityService.findcommunitypost(communityContentNumber);

        // 게시글 숨김 처리 + 어드민만 숨김 글 접근 로직
        if (post != null && post.getCommunityHidden() == 1) {
            if (admin != 1) {
                return "redirect:/community";
            }
            // 관리자(admin == 1)라면 이 if 블록을 건너뛰고 게시글을 정상적으로 보여줍니다.
        }
        communityService.viewCount(communityContentNumber);

        model.addAttribute("post", post);

        // 게시글 본문 투표 현황
        int vote = communityService.vote(communityContentNumber, id);
        model.addAttribute("vote", vote);

        // 댓글 목록 조회
        List<CommunityReplyDTO> reply = communityReplyService.findreply(communityContentNumber, id);
        reply.forEach(replyDTO -> {
            // 1. 부모 댓글 상태 주입
            int status = communityReplyVoteService.vote(replyDTO.getReplyNumber(), id);
            replyDTO.setReplyStatus(status);

            // 2. 대댓글 상태 주입 (children이 null이 아니라고 가정)
            replyDTO.getChildren().forEach(subReplyDTO -> {
                int subStatus = communityReplyVoteService.vote(subReplyDTO.getReplyNumber(), id);
                subReplyDTO.setReplyStatus(subStatus);
            });
        });
        model.addAttribute("reply", reply);

        return "/community/communitypost";
    }

    @PostMapping(value = "/community/like")
    @ResponseBody
    public String contentLike(@RequestParam Long communityContentNumber, @RequestParam String id) {
        communityService.contentlike(communityContentNumber, id);

        return "succes";
    }

    @PostMapping(value = "/community/dislike")
    @ResponseBody
    public String contentDisLike(@RequestParam Long communityContentNumber, @RequestParam String id) {
        communityService.contentdislike(communityContentNumber, id);

        return "succes";
    }

    @GetMapping(value = "/community/communitymodify")
    public String CommunityModify(@RequestParam("communityContentNumber") Long communityContentNumber, HttpServletRequest request, Model model, HttpSession session) {
        model.addAttribute("currentUri", request.getRequestURI());

        CommunityEntity post = communityService.findcommunitypost(communityContentNumber); // 게시글 가져오기
        model.addAttribute("post", post);

        String id = (String) session.getAttribute("id");
        model.addAttribute("id", id);

        return "/community/communitymodify";
    }

    @PostMapping(value = "/community/communitymodifysave")
    public String CommunityModifySave(@RequestParam("modifyFile") MultipartFile modifyimage, HttpServletRequest request, Model model, CommunityDTO communityDTO) throws IOException {
        model.addAttribute("currentUri", request.getRequestURI());

        if(modifyimage != null && !modifyimage.isEmpty()) { // && !modifyimage.isEmpty() -> 파일을 추가하지 않으면 글자가 없으니까 글자 없는거 까지 잡아내려고
            String imagename = modifyimage.getOriginalFilename(); // 새로 넣을 이미지 이름
            String imagefile = UUID.randomUUID().toString() + "_" + imagename; // 새로 넣을 이미지 파일

            File saveFIle = new File("C:\\project\\Data\\community\\image\\" + imagefile); // 새로 넣을 파일
            modifyimage.transferTo(saveFIle); // 실제 경로로 saveFILE 이동 (덮어쓰기)
            communityDTO.setCommunityImage(imagefile); // 새로 들어온 imagefile 컬럼에 넣기
        } else{ // 2. 파일을 업로드하지 않았다. -> 기존 파일을 유지하거나 더미파일을 만들지 않는다.
            communityDTO.setCommunityImage(null); // 파일을 선택하지 않으면 null로 둔다 -> 이후 구현부에서 원래 수정 전 게시물을 불러오는 과정에서 수정 된 이미지가 없으면 수정 전 이미지를 그대로 사용하는 방식임
        }

        CommunityEntity centity = communityDTO.entity();
        communityService.contentmodify(centity);

        return "redirect:/community";
    }

    @PostMapping(value = "/community/communitydelete")
    public String Communitydelete(@RequestParam Long communityContentNumber, HttpServletRequest request, Model model, HttpSession session, CommunityDTO communityDTO) {
        model.addAttribute("currentUri", request.getRequestURI());

        String id = (String) session.getAttribute("id");

        CommunityEntity post = communityService.findcommunitypost(communityContentNumber);
        if (id == null || post == null || !id.equals(post.getId())){

            return "redirect:/community/communitypost?communityContentNumber=" + communityContentNumber;
        }

        communityService.contentdelete(communityContentNumber);

        return "redirect:/community";
    }

    @PostMapping(value = "/community/replymodify")
    @ResponseBody
    public String communityreplymodify(@RequestParam Long replyNumber,
                                       @RequestParam String replyContent,
                                       CommunityReplyDTO communityReplyDTO, Model model) {
        communityReplyService.replymodify(replyNumber, replyContent);

        return "ok";
    }

    @PostMapping(value = "/community/hidepost")
    public String posthide(@RequestParam Long communityContentNumber, HttpSession session){
        String id = (String) session.getAttribute("id");

        MemberEntity member = memberRepository.findById(id).orElse(null);

        if (member == null || member.getAdmin() != 1){

            return "redirect:/login";
        }
        communityService.hidePost(communityContentNumber);
        return "redirect:/community";
    }

    @PostMapping(value = "/community/deletepost")
    public String postdelete(@RequestParam Long communityContentNumber, HttpSession session){
        String id = (String) session.getAttribute("id");
        MemberEntity member = memberRepository.findById(id).orElse(null);

        if (member == null || member.getAdmin() != 1) {
            return "redirect:/login";
        }
        communityService.deletepost(communityContentNumber);
        return "redirect:/community";
    }

    @PostMapping(value = "/community/unhidepost")
    public String postunhide(@RequestParam Long communityContentNumber, HttpSession session){
        String id = (String) session.getAttribute("id");
        MemberEntity member = memberRepository.findById(id).orElse(null);

        if (member == null || member.getAdmin() != 1){
            return "redirect:/login";
        }
        communityService.unhidePost(communityContentNumber);
        return "redirect:/community/communitypost?communityContentNumber=" + communityContentNumber;
    }

//    @GetMapping("/community/user")
//    public String userCommunityList(
//            @RequestParam String userId,
//            @RequestParam(value = "page", defaultValue = "0") int page,
//            Model model, HttpSession session, HttpServletRequest request) {
//
//        model.addAttribute("currentUri", request.getRequestURI());
//        String id = (String) session.getAttribute("id");
//        model.addAttribute("id", id);
//
//        Pageable pageable = PageRequest.of(page, 10);
//        Page<CommunityEntity> userPosts = communityService.getUserCommunity(userId, pageable);
//
//        model.addAttribute("list", userPosts);
//        model.addAttribute("userId", userId);
//
//        return "community/community";
//
////        정확히 말하면 Repository는 List + count,
////                Service에서 Page로 감싸서,
////        Controller → HTML(Thymeleaf)에서는 Page를 사용
////        이 구조가 맞아.
////
////        너 지금 모든 커뮤니티 페이징을 다 이렇게 하고 있음.
//    }

@GetMapping("/community/user")
public String userCommunityList(
        @RequestParam String userId,
        @PageableDefault(size = 10, page = 0) Pageable pageable, // PageableDefault 추가
        Model model, HttpSession session, HttpServletRequest request) {

    model.addAttribute("currentUri", request.getRequestURI());
    String id = (String) session.getAttribute("id");

    MemberEntity member = memberRepository.findById(id).orElse(null);
    int admin = (member != null) ? member.getAdmin() : 0;
    model.addAttribute("id", id);
    model.addAttribute("admin", admin); // admin 변수가 HTML에서 쓰일 수 있음

    Page<CommunityEntity> list = communityService.getUserCommunity(userId, pageable);
    model.addAttribute("list", list);

    int currentPage = list.getPageable().getPageNumber();
    int startPage = Math.max(0, currentPage / 10 * 10);
    int endPage = Math.min(list.getTotalPages() == 0 ? 0 : list.getTotalPages() - 1, startPage + 9);

    model.addAttribute("startPage", startPage);
    model.addAttribute("endPage", endPage);

    model.addAttribute("searchType", "id");
    model.addAttribute("keyword", userId);

    model.addAttribute("currentCategory", null);

    return "community/community";
}

    @PostMapping("/community/banUser")
    @ResponseBody
    public String banUser(@RequestParam String userId, @RequestParam int hours, HttpSession session) {
        String adminId = (String) session.getAttribute("id");
        MemberEntity admin = memberRepository.findById(adminId).orElse(null);

        if(admin == null || admin.getAdmin() != 1){
            return "NO_PERMISSION";
        }

        communityService.banUser(userId, hours);

        return "SUCCESS";
    }
}
