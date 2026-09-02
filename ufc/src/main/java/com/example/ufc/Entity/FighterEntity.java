package com.example.ufc.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "FIGHTER")
@Getter
@Setter
public class FighterEntity {

    @Id
    @Column(name = "NAME")
    private String name;

    // 🌟 Double 타입으로 통일 (DB의 FLOAT/NUMBER와 매핑) 🌟
    @Column(name = "HEIGHT")
    private Double height;

    @Column(name = "WEIGHT")
    private Double weight;

    @Column(name = "REACH")
    private Double reach;

    @Column(name = "STANCE")
    private String stance;

    @Column(name = "DOB")
    private LocalDate dob;

    @Column(name = "SLPM")
    private Double slpm;

    // 🌟 이 필드들이 Schema-validation 오류의 원인이었습니다. Double로 통일 🌟
    @Column(name = "STRACC")
    private Double strAcc;

    @Column(name = "SAPM")
    private Double sapm;

    @Column(name = "STRDEF")
    private Double strDef;

    @Column(name = "TDAVG")
    private Double tdAvg;

    @Column(name = "TDACC")
    private Double tdAcc;

    @Column(name = "TDDEF")
    private Double tdDef;

    @Column(name = "SUBAVG")
    private Double subAvg;

    // ... (나머지 필드)

    @Column(name = "WEIGHT_CLASS")
    private String weightClass;

    @Column(name = "RANK_NUM")
    private String rankNum;

    @Column(name = "WEIGHT_CODE")
    private Integer weightCode;

    @Column(name = "TOTAL")
    private Integer total;

    @Column(name = "WINS")
    private Integer totalWins; // WINS 필드명은 totalWins로 가정

    @Column(name = "KO_TKO")
    private Integer koTko;

    @Column(name = "SUB_WINS")
    private Integer subWins;

    @Column(name = "DEC_WINS")
    private Integer decWins;

    @Column(name = "LOSSES")
    private Integer totalLosses; // LOSSES 필드명은 totalLosses로 가정

    @Column(name = "DRAWS")
    private Integer draws;

    @Column(name = "AVG_TIME")
    private Double avgTime;

    @Column(name = "FIGHTER_CODE")
    private Integer fighterCode;

    @Column(name = "IMAGE_URL")
    private String imageUrl;
}