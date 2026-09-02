package com.example.ufc.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "integratedSearchLog")
@SequenceGenerator(name = "integratedSearchLog",
    sequenceName = "integratedSearchLog_seq",
    initialValue =1000,
    allocationSize =1 )
public class IntegratedSearchLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator ="integratedSearchLog")
    Long log; // 검색 로그 번호 시퀀스
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fighter_entity_name", referencedColumnName = "NAME") //name = "Fighter_NAME",
    FighterEntity fighterEntity; //fighter테이블에 name컬럼에 연결
    @Column(name = "SEARCH_TIME")
    LocalDateTime searchTime; // 검색 시간

    public IntegratedSearchLogEntity(FighterEntity fighterEntity){
        this.fighterEntity = fighterEntity;
        this.searchTime = LocalDateTime.now();
    }
}
