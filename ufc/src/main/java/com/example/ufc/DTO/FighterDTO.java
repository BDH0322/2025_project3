package com.example.ufc.DTO;

import com.example.ufc.Entity.FighterEntity;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class FighterDTO {

    // 1. 기본 정보
    private String name;
    private String weightClass;
    private String rankNum;
    private String imageUrl;

    // 2. 신체 정보 (Double)
    private Double height;
    private Double weight;
    private Double reach;
    private String stance;
    private LocalDate dob;

    // 3. 🌟 통계 필드: 모두 Double로 통일 🌟
    private Double slpm;
    private Double strAcc; // 🌟 String -> Double 🌟
    private Double sapm;
    private Double strDef; // 🌟 String -> Double 🌟
    private Double tdAvg;
    private Double tdAcc;  // 🌟 String -> Double 🌟
    private Double tdDef;  // 🌟 String -> Double 🌟
    private Double subAvg;
    private Double avgTime;

    // 4. 전적 정보 (Integer)
    private Integer total;
    private Integer totalWins;
    private Integer koTko;
    private Integer subWins;
    private Integer decWins;
    private Integer totalLosses;
    private Integer draws;

    // 5. 뷰/로직을 위한 계산된 스탯 (Integer, 0-100점)
    private Integer strikingOffense;
    private Integer strikingDefense;
    private Integer strikingAccuracy;
    private Integer strikingReceived;
    private Integer grapplingOffense;
    private Integer grapplingDefense;
    private Integer grapplingAccuracy;
    private Integer submissionSkill;


    /*현재 사용자님이 작성하신 코드는 Entity(DB 데이터)를 DTO(화면 전달용 데이터)로 바꾸는 로직*/
    public static FighterDTO fromEntity(FighterEntity entity) {
        FighterDTO dto = new FighterDTO();

        // 1. 기본 정보
        dto.setName(entity.getName());
        dto.setWeightClass(entity.getWeightClass());
        dto.setRankNum(entity.getRankNum());
        dto.setImageUrl(entity.getImageUrl());

        // 2. 신체 정보 및 DOB
        dto.setHeight(entity.getHeight());
        dto.setWeight(entity.getWeight());
        dto.setReach(entity.getReach());
        dto.setStance(entity.getStance());
        dto.setDob(entity.getDob());

        // 3. 통계 (Python 계산에 필요한 모든 데이터)
        dto.setSlpm(entity.getSlpm());
        dto.setStrAcc(entity.getStrAcc());
        dto.setSapm(entity.getSapm());
        dto.setStrDef(entity.getStrDef());
        dto.setTdAvg(entity.getTdAvg());
        dto.setTdAcc(entity.getTdAcc());
        dto.setTdDef(entity.getTdDef());
        dto.setSubAvg(entity.getSubAvg());
        dto.setAvgTime(entity.getAvgTime());

        // 4. 전적 정보
        dto.setTotal(entity.getTotal());
        dto.setTotalWins(entity.getTotalWins());
        dto.setKoTko(entity.getKoTko());
        dto.setSubWins(entity.getSubWins());
        dto.setDecWins(entity.getDecWins());
        dto.setTotalLosses(entity.getTotalLosses());
        dto.setDraws(entity.getDraws());

        return dto;
    }
}