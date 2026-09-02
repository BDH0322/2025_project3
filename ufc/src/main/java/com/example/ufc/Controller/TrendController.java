package com.example.ufc.Controller;

import com.example.ufc.DTO.FighterDTO;
import com.example.ufc.Service.TrendService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class TrendController {

    @Autowired
    TrendService trendService;

    @GetMapping({"/trend"})
    public String trend(HttpServletRequest request, Model model){
        String requestUri = request.getRequestURI();
        model.addAttribute("currentUri", requestUri);

        // 3. 통계
        List<FighterDTO> hotFighters = trendService.hotFighters();
        List<FighterDTO> starFighters = trendService.starFighters();
        List<FighterDTO> alertFighters = trendService.alertFighters();

        // 4. 핫 이슈
        List<FighterDTO> heavyChamp = trendService.heavyChamp();
        List<FighterDTO> hotIssue = trendService.hotIssue(3);
        List<FighterDTO> risingStar = trendService.risingStar(3);

        // 6. 디깅
        List<Map<String, Object>> digging = trendService.digging();

        // 통계
        model.addAttribute("hotFighters", hotFighters);
        model.addAttribute("starFighters", starFighters);
        model.addAttribute("alertFighters", alertFighters);

        // 핫 이슈
        model.addAttribute("heavyChamp", heavyChamp);
        model.addAttribute("hotIssue", hotIssue);
        model.addAttribute("risingStar", risingStar);

        // 디깅
        model.addAttribute("digging", digging);

        return "trend/trend";
    }
}
