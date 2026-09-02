package com.example.ufc.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor // Lombok 어노테이션 추가
@AllArgsConstructor // Lombok 어노테이션 추가
public class VoteBoardDTO {
    // 게시글 ID는 DB와 Service에서 관리하므로 입력받지 않음

    private Long boardNum;       // DB에서 자동 생성되어 반환
    private Long fightNum;       // 폼에서 입력받음
    private String title;
    private String content;      // 폼에서 입력받음 (글 내용 및 이미지)
    private String weightClass;  // 폼에서 선택된 체급
    private String fighter1Name; // 폼에서 선택된 선수 1
    private String fighter2Name; // 폼에서 선택된 선수 2

    //이미지
    private String voteImagePath;

    // 결과값 (DB 저장 후 조회 시 사용)
    private int fighter1Votes = 0;
    private int fighter2Votes = 0;
    private LocalDateTime createdAt;
    private boolean isClosed = false; // 투표 마감 여부
}
