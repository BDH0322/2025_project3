package com.example.ufc.Service;

import com.example.ufc.DTO.FighterDTO;

import java.util.List;
import java.util.Map;

public interface TrendService {

    // 3. 통계
    List<FighterDTO> hotFighters();
    List<FighterDTO> starFighters();
    List<FighterDTO> alertFighters();

    // 4. 핫 이슈
    List<FighterDTO> heavyChamp();
    List<FighterDTO> hotIssue(int i);
    List<FighterDTO> risingStar(int i);

    // 6. 디깅
    List<Map<String, Object>> digging();
}
