package com.example.ufc.Entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name= "VOTE_BOARD")

public class VoteBoardEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "VOTE_SEQ")
    @SequenceGenerator(name = "VOTE_SEQ", sequenceName = "VOTE_BOARD_SEQ", allocationSize = 1)
    private Long id;

    @Column(name="BOARD_NUM", unique = true, nullable = false)
    private Long boardNum;

    @Column(name="FIGHT_NUM")
    private Long fightNum;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(columnDefinition = "CLOB")
    // 오라클 환경에서 Lob과 CLOB 정의가 충돌할 수 있으므로 간단하게 처리
    private String content;

    private String weightClass;
    private String fighter1Name;
    private String fighter2Name;

    // [추가] 저장된 이미지의 경로 또는 파일명을 저장하는 컬럼
    @Column(name ="VOTE_IMAGE")
    private  String voteImage;

    private int fighter1Votes = 0;
    private int fighter2Votes = 0;

    // boolean 대응: 오라클은 0/1로 저장됨
    @Column(name = "IS_CLOSED")
    private boolean isClosed = false;

    @Column(name = "CREATE_AT", updatable = false)
    private LocalDateTime createAt = LocalDateTime.now();

    //jpa에서 insert 시점에 현재 시간을 자동으로 넣어주기 위한 설정
    @PrePersist
    public void prePersist(){
        this.createAt = LocalDateTime.now();
    }
}