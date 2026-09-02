package com.example.ufc.Service;

import com.example.ufc.Entity.TotalPredictionEntity;
import com.example.ufc.Entity.VoteBoardEntity;
import com.example.ufc.Repository.TotalPredictionRepository;
import com.example.ufc.Repository.VoteBoardRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
@AllArgsConstructor
@Service

public class TotalPredictionServiceImpl implements TotalPredictionService{

    private final TotalPredictionRepository totalpredictionRepository;
    private final VoteBoardRepository voteBoardRepository;
    private final PredictionService predictionService;//기존 ai서비스


    @Override
    public void saveInitialAiScore(VoteBoardEntity vote){
        //실시간 ai 예측 값 호출
        Map<String,Object> aiResult = predictionService.getPrediction(
                vote.getFighter1Name(),vote.getFighter2Name());
        Double aiWinRate = (Double) aiResult.get("fighter1WinRate");

        TotalPredictionEntity prediction = new TotalPredictionEntity();
        prediction.setVoteBoard(vote);
        prediction.setAiScore(aiWinRate);

        totalpredictionRepository.save(prediction);

    }
    /*
    @Override
    public void finalizePrediction(Long boardNum){
        TotalPredictionEntity prediction = totalpredictionRepository.findById(boardNum)
                .orElseThrow(() ->new RuntimeException("데이터를 찾을 수 없습니다."));
        VoteBoardEntity vote = prediction.getVoteBoard();

        //VoteBoard의 마감 상태 필드(예: isClosed 또는 status)를 확인한다.
        //여기서는 vote.getIsClosed()가 true여야만 진행되도록 설정

        if (!vote.isClosed()) {
            throw new IllegalStateException("해당 게시글은 아직 마감 처리가 되지 않았습니다. 마감 후 분석이 가능합니다.");
        }

        // -- 이후 마감 확인 된 경우 아래 로직 실행
        long total = vote.getFighter1Votes() + vote.getFighter2Votes();
        //유저 투표율 계산
        double userRate = (total >0) ? (double) vote.getFighter1Votes() / total : 0.5;
        prediction.setUserVoteRate(userRate);
        try {
            // 파이썬 딥러닝 서버 호출 로직
            RestTemplate restTemplate = new RestTemplate();
            String fastApiurl = "http://localhost:8000/predict"; //파이썬 서버 주소

            //파이썬으로 보낼 json 데이터 구성
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("ai_score", prediction.getAiScore());
            requestData.put("user_rate", userRate);
            requestData.put("total_votes", total);


            //파이썬 서버 호출 및 결과 응답 받기
            Map<String, Object> response = restTemplate.postForObject(fastApiurl, requestData, Map.class);
            if (response != null && response.containsKey("combined_score")) {
                Double combinedResult = (Double) response.get("combined_score");
                prediction.setCombineScore(combinedResult); //딥러닝 결과 저장
            }
        }catch (Exception e){
            //파이썬 서버가 꺼져있을 경우르 대비한 백업 수식(4:6 가중치)
            System.out.println("AI 서버 연결 실패: "+e.getMessage());
            double backupScore = (prediction.getAiScore() * 0.4) + (userRate * 0.6);
            prediction.setCombineScore(backupScore);
        }
    }*/

    @Override
    @Transactional
    public void finalizePrediction(Long boardNum) {
        // 1. [수정] ID가 아니라 BOARD_NUM으로 게시글을 먼저 찾습니다.
        VoteBoardEntity vote = voteBoardRepository.findByBoardNum(boardNum)
                .orElseThrow(() -> new RuntimeException("게시글 데이터를 찾을 수 없습니다. boardNum: " + boardNum));

        // 2. [수정] 해당 게시글에 연결된 AI 분석 엔티티를 가져옵니다.
        // 없으면 새로 생성합니다. (기존에 saveInitialAiScore가 실행 안 됐을 경우 대비)
        TotalPredictionEntity prediction = totalpredictionRepository.findByVoteBoard(vote)
                .orElse(new TotalPredictionEntity());

        prediction.setVoteBoard(vote);




        // 3. 마감 상태 확인
        if (!vote.isClosed()) {
            throw new IllegalStateException("해당 게시글은 아직 마감 처리가 되지 않았습니다.");
        }

        // 4. 투표 데이터 계산
        //long total = vote.getFighter1Votes() + vote.getFighter2Votes();
        //double userRate = (total > 0) ? (double) vote.getFighter1Votes() / total : 0.5;

        double f1Votes = vote.getFighter1Votes();
        double f2Votes = vote.getFighter2Votes();
        double total = f1Votes + f2Votes;

        double userRate = (total > 0) ? f1Votes / total : 0.5;

        prediction.setUserVoteRate(userRate);

        // 5. 파이썬 서버 호출
        try {
            RestTemplate restTemplate = new RestTemplate();
            String fastApiurl = "http://localhost:8000/predict";

            Map<String, Object> requestData = new HashMap<>();
            // aiScore가 null일 경우를 대비해 기본값 0.5 설정
            requestData.put("ai_score", prediction.getAiScore() != null ? prediction.getAiScore() : 0.5);
            requestData.put("user_rate", userRate);
            requestData.put("total_votes", total);

            Map<String, Object> response = restTemplate.postForObject(fastApiurl, requestData, Map.class);

            if (response != null && response.containsKey("combined_score")) {
                //Double combinedResult = (Double) response.get("combined_score");
                // [수정 핵심] 어떤 숫자 타입이 와도 안전하게 Double로 변환
                double combinedResult = Double.parseDouble(response.get("combined_score").toString());
                prediction.setCombineScore(combinedResult);
            }
        } catch (Exception e) {
            System.err.println("AI 서버 통신 실패, 백업 수식 가동: " + e.getMessage());
            // 백업 수식: AI(40%) + 유저(60%)
            double aiScore = prediction.getAiScore() != null ? prediction.getAiScore() : 0.5;
            double backupScore = (aiScore * 0.4) + (userRate * 0.6);
            prediction.setCombineScore(backupScore);
        }

        // 6. 결과 저장
        totalpredictionRepository.save(prediction);
    }

    @Override
    public void updateActualWinner(Long boardNum, Integer winner){
        TotalPredictionEntity prediction = totalpredictionRepository.findById(boardNum).get();
        prediction.setActualWinner(winner);
        //이제 이행(row)은 완벽한 딥러닝 학습 데이터가 완성 됨
    }



}
