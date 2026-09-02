package com.example.ufc.Service;


import com.example.ufc.DTO.FighterDTO;
import org.springframework.stereotype.Service;
import com.example.ufc.Repository.FighterRepository;

import java.util.List;
import java.util.Map;

/**
 * 랭킹 페이지 비즈니스 로직에 대한 인터페이스 정의
 */
@Service
public interface FighterService {
     String getImgUrlByName(String name);
     FighterDTO calculateAndSetPentagonStats(FighterDTO dto);
     Map<String, List<FighterDTO>> getRankingsByWeightClass();
     Map<String, Object> getProcessedFighterDetail(String name);



     List<FighterDTO> fighterRanking();

     Map<String, Object> metadata();
}
