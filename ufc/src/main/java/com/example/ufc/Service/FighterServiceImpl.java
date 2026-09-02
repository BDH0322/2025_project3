package com.example.ufc.Service;

import com.example.ufc.DTO.FighterDTO;
import com.example.ufc.Entity.FighterEntity;
import com.example.ufc.Repository.FighterRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter; // 사용되지 않아도 임포트 유지
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FighterServiceImpl implements FighterService {
    private Map<String,Double> globalAverages = null;
    private final FighterRepository fighterRepository;

    public FighterServiceImpl(FighterRepository fighterRepository) {
        this.fighterRepository = fighterRepository;
    }

    /**
     * Entity(DB 데이터)를 DTO(뷰 데이터)로 변환하는 로직.
     */
    private FighterDTO convertToDto(FighterEntity fighter) {
        FighterDTO dto = new FighterDTO();

        // 1. 필수 및 String 타입 필드 복사
        dto.setName(fighter.getName());
        dto.setWeightClass(fighter.getWeightClass());
        dto.setRankNum(fighter.getRankNum());
        dto.setImageUrl(fighter.getImageUrl());

        // 2. Double 타입 필드 복사 (신체 및 통계)
        dto.setHeight(fighter.getHeight());
        dto.setWeight(fighter.getWeight());
        dto.setReach(fighter.getReach());
        dto.setStance(fighter.getStance());

        // 🌟🌟🌟 Entity와 DTO가 모두 Double이므로, 타입 오류 없이 바로 복사 🌟🌟🌟
        dto.setStrAcc(fighter.getStrAcc());
        dto.setStrDef(fighter.getStrDef());
        dto.setTdAcc(fighter.getTdAcc());
        dto.setTdDef(fighter.getTdDef());
        // ----------------------------------------------------

        dto.setSlpm(fighter.getSlpm());
        dto.setSapm(fighter.getSapm());
        dto.setTdAvg(fighter.getTdAvg());
        dto.setSubAvg(fighter.getSubAvg());

        // 3. DOB 복사
        dto.setDob(fighter.getDob());

        // 4. 전적 Integer 필드 복사
        dto.setTotal(fighter.getTotal());
        dto.setTotalWins(fighter.getTotalWins());
        dto.setKoTko(fighter.getKoTko());
        dto.setSubWins(fighter.getSubWins());
        dto.setDecWins(fighter.getDecWins());
        dto.setTotalLosses(fighter.getTotalLosses());
        dto.setDraws(fighter.getDraws());

        // 5. AVG_TIME 복사
        dto.setAvgTime(fighter.getAvgTime());

        return dto;
    }

    @Override
    public String getImgUrlByName(String name){
        return fighterRepository.findByName(name)
                .map(FighterEntity::getImageUrl)
                .orElse("/images/default_fighter.png"); // 없으면 기본이미지

    }
    @Override
    public Map<String, List<FighterDTO>> getRankingsByWeightClass() {

        List<FighterEntity> allFighters = fighterRepository.findAll();

        List<FighterDTO> allFighterDtos = allFighters.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        // 1. 랭킹 순위 정렬 Comparator (체급 내 순위 정렬)
        Comparator<FighterDTO> rankComparator = (f1, f2) -> {
            String rank1 = f1.getRankNum();
            String rank2 = f2.getRankNum();
            if ("C".equals(rank1)) return -1;
            if ("C".equals(rank2)) return 1;

            try {
                return Integer.compare(Integer.parseInt(rank1), Integer.parseInt(rank2));
            } catch (NumberFormatException e) {
                return rank1.compareTo(rank2);
            }
        };

        // 2. 체급별 그룹화 및 체급 내 순위 정렬
        Map<String, List<FighterDTO>> groupedRankings = allFighterDtos.stream()
                .collect(Collectors.groupingBy(
                        FighterDTO::getWeightClass,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    list.sort(rankComparator);
                                    return list;
                                }
                        )
                ));

        // 3. 체급 순서를 정의하고 LinkedHashMap으로 순서를 보장하여 반환
        List<String> weightClassOrder = List.of(
                "Featherweight",
                "Lightweight",
                "Welterweight",
                "Middleweight",
                "Light Heavyweight",
                "Heavyweight"
                // 만약 다른 체급이 있다면 여기에 추가하면 됩니다.
        );

        Map<String, List<FighterDTO>> orderedRankings = new LinkedHashMap<>();

        weightClassOrder.forEach(weightClass -> {
            // DB에 데이터가 있는 체급만 Map에 추가
            if (groupedRankings.containsKey(weightClass)) {
                orderedRankings.put(weightClass, groupedRankings.get(weightClass));
            }
        });

        return orderedRankings;
    }


    public FighterDTO getFighterDetailByName(String name) {

        Optional<FighterEntity> fighterOptional = fighterRepository.findById(name);

        return fighterOptional
                .map(this::convertToDto)
                .orElse(null);
    }


    // 🌟🌟🌟 String을 Double로 파싱하는 함수는 더 이상 필요 없으므로 삭제/주석 처리함 🌟🌟🌟
    /*
    private double parseStatValue(String statStr) {
        if (statStr == null || statStr.isEmpty()) return 0.0;
        try {
            return Double.parseDouble(statStr.replace("%", "").trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    */

    /**
     * DTO의 원본 통계를 기반으로 4축 마름모 차트의 8가지 스탯 (0-100점)을 계산하여 DTO에 설정합니다.
     * @param dto DB에서 가져온 원본 통계가 담긴 FighterDTO
     * @return 8가지 계산된 스탯이 추가된 FighterDTO
     */
    @Override
    public FighterDTO calculateAndSetPentagonStats(FighterDTO dto) {

        // 1. 원본 통계 값 안전하게 추출 (모두 Double 타입)
        double slpm = dto.getSlpm() != null ? dto.getSlpm() : 0.0;
        double strAcc = dto.getStrAcc() != null ? dto.getStrAcc() : 0.0; // 🌟 DTO가 Double이므로 바로 사용
        double sapm = dto.getSapm() != null ? dto.getSapm() : 0.0;
        double strDef = dto.getStrDef() != null ? dto.getStrDef() : 0.0; // 🌟 DTO가 Double이므로 바로 사용
        double tdAvg = dto.getTdAvg() != null ? dto.getTdAvg() : 0.0;
        double tdAcc = dto.getTdAcc() != null ? dto.getTdAcc() : 0.0;  // 🌟 DTO가 Double이므로 바로 사용
        double tdDef = dto.getTdDef() != null ? dto.getTdDef() : 0.0;  // 🌟 DTO가 Double이므로 바로 사용
        double subAvg = dto.getSubAvg() != null ? dto.getSubAvg() : 0.0;

        // 2. 🌟 타격 스탯 계산 및 정규화 (0-100점) 🌟

        // 공격성 (SLpM): Max 10.0 가정
        dto.setStrikingOffense((int) Math.min(100, slpm * 10));

        // 방어 (Str. Def): Max 100% (이미 0-100 사이 값)
        dto.setStrikingDefense((int) Math.min(100, strDef));

        // 정확도 (Str. Acc): Max 100%
        dto.setStrikingAccuracy((int) Math.min(100, strAcc));

        // 피격 방어 (SApM): 낮을수록 좋음 (Max 10.0 가정, 역산)
        dto.setStrikingReceived((int) Math.max(0, 100 - (sapm * 10)));

        // 3. 🌟 그라운드 스탯 계산 및 정규화 (0-100점) 🌟

        // TD 공격 (TD Avg.): Max 10.0 가정
        dto.setGrapplingOffense((int) Math.min(100, tdAvg * 10));

        // TD 방어 (TD Def.): Max 100%
        dto.setGrapplingDefense((int) Math.min(100, tdDef));

        // TD 정확도 (TD Acc.): Max 100%
        dto.setGrapplingAccuracy((int) Math.min(100, tdAcc));

        // 서브미션 (Sub Avg.): Max 5.0 가정
        dto.setSubmissionSkill((int) Math.min(100, subAvg * 20));

        return dto;
    }
    private Double calculateAverage(List<FighterEntity> fighters,java.util.function.Function<FighterEntity,Double>getter){
        return fighters.stream()
                .map(getter)
                .filter(d->d!=null)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    private void calculateGlobalAverage(List<FighterEntity> allFighters){
        if(globalAverages != null) return;

        if(allFighters.isEmpty()){
            globalAverages = Map.of();
            return;
        }
        Map<String,Double> newAverages = new HashMap<>();
        newAverages.put("slpm", calculateAverage(allFighters, FighterEntity::getSlpm));
        newAverages.put("strAcc", calculateAverage(allFighters, FighterEntity::getStrAcc));
        newAverages.put("sapm", calculateAverage(allFighters, FighterEntity::getSapm));
        newAverages.put("strDef", calculateAverage(allFighters, FighterEntity::getStrDef));
        newAverages.put("tdAvg", calculateAverage(allFighters, FighterEntity::getTdAvg));
        newAverages.put("tdAcc", calculateAverage(allFighters, FighterEntity::getTdAcc));
        newAverages.put("tdDef", calculateAverage(allFighters, FighterEntity::getTdDef));
        newAverages.put("subAvg", calculateAverage(allFighters, FighterEntity::getSubAvg));

        globalAverages = newAverages;

    }


    /**
     * 3. 🌟 DTO에 비율을 설정하는 대신, 비율 Map을 반환합니다. 🌟
     *
     * @return 8가지 통계의 글로벌 비교 비율을 담은 Map<String, Double>
     */
    private Map<String, Double> calculateComparisonRatios(FighterDTO dto) {
        return null;
    }
    /**
     * 4. 컨트롤러에서 호출할 상세 정보 통합 처리 메소드
     * @return DTO와 ratios Map을 담은 Map<String, Object>
     */
    @Override
    public Map<String, Object> getProcessedFighterDetail(String name) {

        List<FighterEntity> allFighters = fighterRepository.findAll();
        calculateGlobalAverages(allFighters);

        FighterDTO dto = getFighterDetailByName(name);

        if (dto == null) {
            return null;
        }

        // 펜타곤(다이아몬드) 스탯 계산
        dto = calculateAndSetPentagonStats(dto);

        // 글로벌 비교 비율 계산 (Map으로 반환)
        Map<String, Double> ratios = calculateComparisonRatios(dto);

        // DTO와 비율 Map을 통합하여 Controller로 반환
        Map<String, Object> result = new HashMap<>();
        result.put("fighter", dto);
        result.put("ratios", ratios);

        return result;
    }

    private void calculateGlobalAverages(List<FighterEntity> allFighters) {
    }

    @Override
    public List<FighterDTO> fighterRanking() {
        int weightCode = new Random().nextInt(6) + 1;

        return fighterRepository.findByWeightCode(weightCode)
                .stream()
                .map(FighterDTO::fromEntity)
                .toList();
    }

    @Override
    public Map<String, Object> metadata() {
        List<FighterEntity> champions = fighterRepository.champions();
        if (champions == null || champions.isEmpty()) return null;

        double avgStrAcc = champions.stream().mapToDouble(FighterEntity::getStrAcc).average().orElse(0);
        double avgTdAvg = champions.stream().mapToDouble(FighterEntity::getTdAvg).average().orElse(0);
        double avgSubAvg = champions.stream().mapToDouble(FighterEntity::getSubAvg).average().orElse(0);

        // 랜덤 챔피언(새로고침)
        Collections.shuffle(champions);
        FighterEntity champ = champions.get(0);

        // 선수수치 - 평균
        double strAccGap = champ.getStrAcc() - avgStrAcc;
        double tdGap = champ.getTdAvg() - avgTdAvg;

        // 그래플링 가중치 (테이크 다운 + 서브미션 * 1.5)
        double metaScore = avgTdAvg + (avgSubAvg * 1.5);

        Map<String, Object> meta = new HashMap<>();
        meta.put("champ", champ);
        meta.put("avgStrAcc", Math.round(avgStrAcc));
        meta.put("strAccGap", Math.round(strAccGap * 10) / 10.0); // 예: +3.5
        meta.put("tdGap", Math.round(tdGap * 10) / 10.0);
        meta.put("metaTitle", metaScore > 2.5 ? "그래플링 & 서브미션 메타" : "정밀 타격 & 운영 메타");

        return meta;
    }

}