package com.example.ufc.Controller;

import com.example.ufc.DTO.FighterDTO;
import com.example.ufc.Service.PredictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/predict")
public class PredictionController {
    @Autowired
    private PredictionService predictionService;

    //[1] 체급별 선수 목록 요청 중계(ajax)
    @GetMapping("/fighters/{weightClass}")
    public ResponseEntity<?> getFighters(@PathVariable String weightClass){
        try{
            // List<String> 대신 List<FighterDetailObject> 반환
            List<FighterDTO> fighters = predictionService.getFightersByWeight(weightClass);
            return ResponseEntity.ok(fighters);
        } catch(RuntimeException e){
            // Service에서 던진 RuntimeException을 잡아서 처리합니다.
            // 이로 인해 HTML 에러 페이지가 아닌 JSON 오류 응답을 클라이언트에게 보냅니다.
            System.err.println("Controller에서 서비스 오류 처리: " + e.getMessage());
            //500 Internal Server Error 상태 코드를 반환하여 json 오류 메시지를 담아 보냄
            Map<String,String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage()); // Service에서 던진 상세 오류 메시지 사용
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/analyze")
    public Map<String,Object> analyzeMatch(@RequestBody Map<String,String> payload){
        String fighter1 = payload.get("fighter1");
        String fighter2 = payload.get("fighter2");
        return predictionService.getPrediction(fighter1,fighter2);

    }
}
