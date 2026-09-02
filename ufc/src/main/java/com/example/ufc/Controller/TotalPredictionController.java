package com.example.ufc.Controller;


import com.example.ufc.Service.TotalPredictionService;
import com.example.ufc.Service.VoteBoardService;
import jakarta.persistence.PrePersist;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/total-prediction")
@RequiredArgsConstructor
public class TotalPredictionController {

    private final TotalPredictionService totalPredictionService;
    private final VoteBoardService voteBoardService;

    /**
     [1] 투표 마감 및 종합 예측 확정
     관리자가 마감 버튼을 누르면 유저 투표율을 박제하고 딥러닝 결과를 도출
     */
    @PostMapping("/close/{boardNum}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> closeAndAnalyze(@PathVariable Long boardNum){


        //2.AI점수 + 최종 유저 투표율을 바탕으로 딥러닝 결과 계산 및 저장
        totalPredictionService.finalizePrediction(boardNum);

        return ResponseEntity.ok("투표가 마감되었으며, 딥러닝 종합 예측이 완료되었습니다.");
    }

    /**
    * [2] 실제 경가 결과 기록(딥러닝 학습의 '정답' 입력)
     * @param winner 1:RED 승리, 2:BLUE 승리
     * */

    @PostMapping("/confirm-winner")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> setActualWinner(@RequestParam Long boardNum, @RequestParam Integer winner){
        //  실제 승자를 기록하여 나중에 딥러닝 모델 재학습
        totalPredictionService.updateActualWinner(boardNum,winner);
        return ResponseEntity.ok("실제 승자 데이터가 저장되었습니다.(학습 데이터 확보 완료)");
    }
}
