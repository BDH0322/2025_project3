package com.example.ufc.Service;

import com.example.ufc.DTO.FighterDTO;
import com.example.ufc.Entity.FighterEntity;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.example.ufc.Repository.FighterRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


// 🌟 구현 클래스에 @Service를 붙여 Bean으로 등록합니다.
@Service
public class PredictionServiceImpl implements PredictionService{
    private final String PYTHON_API_BASE_URL = "http://localhost:5000/api";




    // 🌟 @Autowired 필드 주입 대신 final 필드로 선언
    private final RestTemplate restTemplate;
    private final FighterServiceImpl fighterService;


    // 🌟 생성자 주입 (Spring이 RestTemplate Bean이 없으면 이 단계에서 명확한 오류를 발생시킵니다.)
    public PredictionServiceImpl(RestTemplate restTemplate, FighterServiceImpl fighterService){
        this.restTemplate = restTemplate;
        this.fighterService = fighterService;
    }

    /*Override
    public List<Fighter> getFightersByWeight(String weightClass){
       try{
        String url = PYTHON_API_BASE_URL +"/fighters/"+weightClass;
        ResponseEntity<List<String>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<String>>() {}

        );
        if(!response.getStatusCode().is2xxSuccessful()){
            throw new RuntimeException("Flask 서버에서 http 오류 응답:"+ response.getStatusCode());
        }
        return response.getBody();
       }catch (Exception e){
           System.err.println("Flask API 호출 중 심각한 오류 발생: " + e.getMessage());
           e.printStackTrace(); // 🌟 로그를 콘솔에 강제로 출력 // 🌟 이 부분이 상세 로그를 출력합니다.
           // 🚨 JavaScript에서 catch 블록으로 진입하게 유도하는 예외 재발생
           throw new RuntimeException("선수 목록 로드 실패 (서버 연결/통신 오류). Spring 콘솔을 확인하세요.");
       }


    }
    */
    @Override
    public List<FighterDTO> getFightersByWeight(String weightClass) {

        // 1. 전체 랭킹 맵을 가져옵니다.
        // Map<String, List<FighterDTO>>
        Map<String, List<FighterDTO>> allRankings = fighterService.getRankingsByWeightClass();

        //2.요청된 체급에 해당하는 List<FighterDTO>를 추출한다.
        List<FighterDTO> fighters = allRankings.getOrDefault(weightClass,List.of());
        // 💡 getOrDefault를 사용하여 해당 체급이 없으면 빈 리스트(List.of())를 반환합니다.
        if (fighters.isEmpty()) {
            // 데이터가 없을 때 빈 리스트 반환 (이전에 겪었던 오류 방지)
            System.out.println("DEBUG: Fighter data not found for weight class: " + weightClass);
        }

        return fighters;
    }


    @Override
    public Map<String,Object> getPrediction(String fighter1,String fighter2){

        Map<String,String> requestBody = new HashMap<>();
        requestBody.put("fighter1",fighter1);
        requestBody.put("fighter2",fighter2);

        try{
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    PYTHON_API_BASE_URL +"/predict",
                    requestBody,
                    Map.class
            );

            if(response.getStatusCode().is2xxSuccessful()){
                return response.getBody();
            } else{
                Map<String,Object> errorMap = new HashMap<>();
                errorMap.put("error","AI예측 중 오류 발생.(파이썬 응답 코드:" + response.getStatusCode() + ")");
                return errorMap;
            }

        }catch (Exception e){
            Map<String,Object> errorMap = new HashMap<>();
            errorMap.put("error","AI 예측 서버(Python:5000) 연결 실패. 서버 실행 상태를 확인하세요.");
            return errorMap;
        }
    }
}
