package com.example.ufc.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "VOTE_HISTORY")
public class VoteHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vote_seq_gen")
    @SequenceGenerator(name = "vote_seq_gen", sequenceName = "VOTE_HISTORY_SEQ", allocationSize = 1)
    private Long id; // 식별용 순번 (오라클 권장 방식)

    @Column(name = "BOARD_NUM")
    private Long boardNum; // 투표 게시글 번호

    @Column(name = "USER_ID")
    private String userId; // 투표 유저 아이디

    @Column(name = "VOTE_TIME")
    private LocalDateTime voteTime; // 투표 시간 로그

    // 서비스에서 새 투표 저장 시 사용할 생성자
    public VoteHistoryEntity(Long boardNum, String userId) {
        this.boardNum = boardNum;
        this.userId = userId;
        this.voteTime = LocalDateTime.now(); // 객체 생성 시 현재 시간 자동 기록
    }

    // DB 저장 전 시간을 자동으로 세팅해주는 메서드
    @PrePersist
    public void prePersist() {
        if (this.voteTime == null) {
            this.voteTime = LocalDateTime.now();
        }
    }
}