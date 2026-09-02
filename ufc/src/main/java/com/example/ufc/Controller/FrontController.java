package com.example.ufc.Controller;

import com.example.ufc.DTO.FighterDTO;
import com.example.ufc.DTO.IntegratedSearchDTO;
import com.example.ufc.Entity.CommunityEntity;
import com.example.ufc.Service.CommunityService;
import com.example.ufc.Service.FighterService;
import com.example.ufc.Service.IntegratedSearchService;
import com.example.ufc.Service.NlpService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
public class FrontController {
    @Autowired
    FighterService fighterService;

    @Autowired
    CommunityService communityService;

    @Autowired
    IntegratedSearchService integratedSearchService;

    @Autowired
    NlpService nlpService;
    @GetMapping(value = {"/","/main"})
    public String handleNavigation(Model model, HttpServletRequest request) {

        // 1. 현재 요청 경로(예: "/", "/player")를 request 객체에서 가져옵니다.
        String requestUri = request.getRequestURI();

        // 2. 이 경로를 'currentUri'라는 이름으로 Thymeleaf에 전달합니다.
        model.addAttribute("currentUri", requestUri);

        //    (추후 /player는 player.html 등으로 변경해야 합니다.)
        // 3. 모든 경로에 대해 'main.html' 템플릿을 반환합니다.

        List<FighterDTO> fighterRanking = fighterService.fighterRanking(); // main - 1. 선수 순위
        List<CommunityEntity> post = communityService.postlist(); // main - 3. HOT 인기 게시글
        Map<String, Object> metadata = fighterService.metadata();
        IntegratedSearchDTO results = integratedSearchService.IntegratedSearch("", 0, 5);
        List<Object[]> rankList = nlpService.getFighterRanking(); // NlpService는 있지만 설계상 dto 없이 service에서 object[]로 만들었기 때문에 object[]로

        model.addAttribute("fighterRanking", fighterRanking);
        model.addAttribute("post", post);
        model.addAttribute("metadata", metadata);
        model.addAttribute("results", results);
        model.addAttribute("rankList", rankList);
        return "main";
    }

//    @GetMapping({"/trend"})
//    public java.lang.String trend(HttpServletRequest request, Model model)
//    {
//        String requestUri = request.getRequestURI();
//
//        // 2. 이 경로를 'currentUri'라는 이름으로 Thymeleaf에 전달합니다.
//        model.addAttribute("currentUri", requestUri);
//
//        return "trend/trend";
//    }


    @GetMapping({"/predictAI"})
    public java.lang.String predictAI(HttpServletRequest request, Model model)
    {
        String requestUri = request.getRequestURI();

        // 2. 이 경로를 'currentUri'라는 이름으로 Thymeleaf에 전달합니다.
        model.addAttribute("currentUri", requestUri);

        return "predict/predictAI";
    }

    @GetMapping(value = "/FrontController/integratedSearch")
    public String integratedSearch(HttpServletRequest request, Model model,
                                   @RequestParam String keyword,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "5") int size) {
        model.addAttribute("currentUri", request.getRequestURI());

        if (keyword == null || keyword.trim().length() < 2) {
            return "redirect:/main"; // 혹은 에러 페이지
        }
        IntegratedSearchDTO results = integratedSearchService.IntegratedSearch(keyword, page, size);
        model.addAttribute("results", results);
        model.addAttribute("keyword", keyword);
        return "integratedSearch";
    }



}
