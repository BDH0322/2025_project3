package com.example.ufc.Service;

import com.example.ufc.DTO.FighterDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

public interface PredictionService {
    //Python api를 호출하여 특정 체급의 선수 목록을 가져온다.
    List<FighterDTO> getFightersByWeight(String weightClass);
    //Python api를 호출하여 두 선수의 승률 예측 결과를 받아들임
    Map<String,Object> getPrediction(String fighter1, String fighter2);

}
