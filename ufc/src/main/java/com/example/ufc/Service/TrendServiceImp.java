package com.example.ufc.Service;

import com.example.ufc.DTO.FighterDTO;
import com.example.ufc.Entity.FighterEntity;
import com.example.ufc.Repository.FighterRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TrendServiceImp implements TrendService {

    @Autowired
    FighterRepository fighterRepository;

    ObjectMapper objectMapper = new ObjectMapper();

    // 통계에서 사용할 메서드, 컬럼 데이터 변경이 아니고 조건으로 축출하는거라 리포지토리 x
    List<FighterDTO> TrendFighters(String scoreType) {
        List<FighterEntity> allFighters = fighterRepository.findAll();
        List<FighterDTO> payload = allFighters.stream()
                .map(FighterDTO::fromEntity)
                .collect(Collectors.toList());

        List<Map<String, Object>> mapPayload = payload.stream()
                .map(dto -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", dto.getName());
                    map.put("height", dto.getHeight());
                    map.put("weight", dto.getWeight());
                    map.put("reach", dto.getReach());
                    map.put("stance", dto.getStance());

                    // 🌟 DOB 필드는 아예 복사하지 않아 문제를 원천 차단 🌟

                    map.put("slpm", dto.getSlpm());
                    map.put("strAcc", dto.getStrAcc());
                    map.put("sapm", dto.getSapm());
                    map.put("strDef", dto.getStrDef());
                    map.put("tdAvg", dto.getTdAvg());
                    map.put("tdAcc", dto.getTdAcc());
                    map.put("tdDef", dto.getTdDef());
                    map.put("subAvg", dto.getSubAvg());
                    map.put("weightClass", dto.getWeightClass());
                    map.put("rankNum", dto.getRankNum());
//                    map.put("weightCode", dto.getWeightCode());
                    map.put("total", dto.getTotal());
                    map.put("totalWins", dto.getTotalWins());
                    map.put("koTko", dto.getKoTko());
                    map.put("subWins", dto.getSubWins());
                    map.put("decWins", dto.getDecWins());
                    map.put("totalLosses", dto.getTotalLosses());
                    map.put("draws", dto.getDraws());
                    map.put("avgTime", dto.getAvgTime());
//                    map.put("fighterCode", dto.getFighterCode());
                    map.put("imageUrl", dto.getImageUrl());
                    return map;
                })
                .collect(Collectors.toList());
        String json;
        try {
            // JsonProcessingException 발생 가능
            json = objectMapper.writeValueAsString(mapPayload);
        } catch (Exception e) {
            // 사용자에게는 빈 리스트를 반환하거나 RuntimeException을 던질 수 있음
            throw new RuntimeException("JSON 변환 오류 발생", e);
        }

        String pythonPath = "C:\\project\\TrendFighters.py"; // path
            try {
                ProcessBuilder processBuilder = new ProcessBuilder("python", pythonPath, scoreType);
                Process process = processBuilder.start();

            try(OutputStream os = process.getOutputStream();
                PrintWriter printWriter = new PrintWriter(os)){
                printWriter.write(json);
                printWriter.flush();
            }

            String errorResult; // 오류로그 : Python의 오류 스트림(stderr)을 읽습니다.
            try(BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream(), "UTF-8"))){
                errorResult = errorReader.lines().collect(Collectors.joining());
            }

            String resultJson; // 오류로그 : 기존 resultJson 읽는 로직
            try(BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), "UTF-8"))){
                resultJson = bufferedReader.lines().collect(Collectors.joining());
            }

            process.waitFor(); // 오류로그 : 오류가 있다면 Java 콘솔에 출력하고 RuntimeException을 발생시킵니다.
            if (!errorResult.isEmpty()) {
                System.err.println("❌ Python 스크립트 오류 (stderr): " + errorResult);
                throw new RuntimeException("Python 스크립트 실행 중 내부 오류: " + errorResult);
            }

            List<FighterDTO> topFighters = objectMapper.readValue(resultJson, new TypeReference<List<FighterDTO>>() {});

            Collections.shuffle(topFighters, new Random());

            if (topFighters.isEmpty()) {
                return Collections.emptyList();
            }

            return topFighters.subList(0, topFighters.size() > 3 ? 3 : topFighters.size());

        } catch (Exception e) {
            System.err.println("❌ Python 통신 중 치명적인 오류: " + e.getMessage());
            throw new RuntimeException("파이썬 통계 데이터 로드 실패", e);
        }
    }


    // 3-1 통계 : 핫 한 선수
    @Override
    public List<FighterDTO> hotFighters() {
        return TrendFighters("hotFighters");
    }

    // 3-2 통계 : 차세대
    @Override
    public List<FighterDTO> starFighters() {
        return TrendFighters("starFighters");
    }

    // 3-3 통계 : 불효자
    @Override
    public List<FighterDTO> alertFighters() {
        return TrendFighters("alertFighters");
    }

    // 4-1. 지구에서 가장 강한 인간: 헤비급 챔피언 조회
    @Override
    public List<FighterDTO> heavyChamp() {
        FighterEntity champ = fighterRepository.heavyChamp();
        if (champ != null) {
            return Collections.singletonList(FighterDTO.fromEntity(champ));
        }
        return Collections.emptyList();
    }

    // 4-2. 핫 이슈 선수: 승률이 가장 높은 선수 (상위 N명)
    @Override
    public List<FighterDTO> hotIssue(int limit) {
        List<FighterEntity> hotIssue = fighterRepository.hotIssue();

        return hotIssue.stream()
                .map(FighterDTO::fromEntity)
                .limit(limit)
                .collect(Collectors.toList());
    }

    // 4-3. 떠오르는 별: 승률이 높고 나이가 어린 선수 (상위 N명)
    @Override
    public List<FighterDTO> risingStar(int limit) {
        List<FighterEntity> risingStar = fighterRepository.risingStar();

        return risingStar.stream()
                .map(FighterDTO::fromEntity)
                .limit(limit)
                .collect(Collectors.toList());
    }

    // 6. 디깅 기준 = map 구조 사용해서 
    private static final Map<String, String> DIGGING_CRITERIA = Map.of(
            "findDiggingQuickFinish", "속전속결",
            "findDiggingHighKnockout", "예절 주입기",
            "findDiggingHighSlpm", "빠른 손",
            "findDiggingHighSapm", "마조히스트",
            "findDiggingHighTdAcc", "대걸레",
            "findDiggingStunGun", "스턴건",
            "findDiggingHighWinRate", "모범생",
            "findDiggingLowWinRate", "양아치"
    );

    @Override
    public List<Map<String, Object>> digging() {
        Random random = new Random(); // 3개 씩 뽑아주는데 랜덤으로

        List<String> weightClass = fighterRepository.weightClass(); // 체급도 랜덤이라 체급 데이터 넣기
        if (weightClass.isEmpty()) return Collections.emptyList();

        List<String> criteriaKey = new ArrayList<>(DIGGING_CRITERIA.keySet());
        Collections.shuffle(criteriaKey);
        List<String> selectedCriteriaKey = criteriaKey.stream().limit(3).collect(Collectors.toList());

        List<Map<String, Object>> resultFighters = new ArrayList<>();

        for (String methodKey : selectedCriteriaKey) {
            try {
                String randomWeightClass = weightClass.get(random.nextInt(weightClass.size()));
                Method repositoryMethod = FighterRepository.class.getMethod(methodKey, String.class);
                List<FighterEntity> topFighters = (List<FighterEntity>) repositoryMethod.invoke(fighterRepository, randomWeightClass);

                if (!topFighters.isEmpty()) {

                    FighterEntity selectedFighter = topFighters.get(random.nextInt(topFighters.size()));
                    FighterDTO dto = FighterDTO.fromEntity(selectedFighter);

                    String criteriaName = DIGGING_CRITERIA.get(methodKey);

                    Map<String, Object> diggingResult = new HashMap<>();
                    diggingResult.put("fighter", dto);
                    diggingResult.put("criteriaName", criteriaName);

                    resultFighters.add(diggingResult);
                }
            } catch (Exception e) {
                System.err.println("디깅 카드 생성 실패 (Repository/Reflection 오류): " + methodKey + " - " + e.getMessage());
            }
        }

        return resultFighters;
    }

}
