package com.example.ufc.Controller;


import com.example.ufc.DTO.FighterDTO;
import com.example.ufc.Service.FighterService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@Controller
public class FighterController {
    private final FighterService fighterService;

    public FighterController(FighterService fighterService) {
        this.fighterService = fighterService;
    }

    @GetMapping("/rank")
    public String showRankPage(Model model, HttpServletRequest request) {

        // 👈 Map 타입 변경: Map<String, List<FighterDTO>>
        Map<String, List<FighterDTO>> rankings = fighterService.getRankingsByWeightClass();

        model.addAttribute("rankings", rankings);

        String currentUri = request.getRequestURI();
        model.addAttribute("currentUri", currentUri);

        return "rank";
    }

    @GetMapping("/fighter/{name}")
    // 🌟 이 어노테이션을 추가합니다 🌟
    @SuppressWarnings("unchecked")
    public String showFighterDetail(@PathVariable("name") String name, Model model, HttpServletRequest request) { // 🌟 mo 대신 model 사용 🌟

        // Service에서 DTO와 비교 비율 Map을 포함하는 Map을 반환 받습니다.
        Map<String, Object> processedData = fighterService.getProcessedFighterDetail(name);

        if (processedData == null || processedData.get("fighter") == null) {
            // 🌟 model 사용 및 에러 메시지 띄어쓰기 수정 🌟
            model.addAttribute("message", "선수 정보를 찾을 수 없습니다: " + name);
            return "error/404";
        }

        // DTO와 비교 비율 Map을 Model에 분리하여 추가합니다.
        model.addAttribute("fighter", (FighterDTO) processedData.get("fighter"));
        model.addAttribute("ratios", (Map<String, Double>) processedData.get("ratios"));

        model.addAttribute("currentUri", request.getRequestURI());

        // 템플릿 경로가 올바른지 다시 한번 확인합니다.
        // statdetail.html이 /templates/detail/ 안에 있다면 이 경로는 올바릅니다.
        return "/detail/statdetail";
    }
}
