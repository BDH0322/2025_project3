package com.example.ufc.Entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "total_prediction")
@Getter
@Setter

public class TotalPredictionEntity {
    @Id
    private Long boardNum; // VoteBoard pk와 공유

    @OneToOne
    @MapsId //voteBoardEntity의 id를 이 엔티티의 pk로 사용

    @JoinColumn(name = "board_num")
    private VoteBoardEntity voteBoard;

    //ai 단돈 예측값
    private Double aiScore;

    //마감 시점의 유저 투표율(0.0 ~ 1.0)
    private Double userVoteRate;

    //딥러닝과 결과값(마감 전에는 0.0으로 관리)
    private Double combineScore = 0.0;

    //실제 경기 결과 (나중에 학습용 정답 데이터로 사용
    //경기전 0, 1:red승 2:blue승
    private Integer actualWinner = 0;
}
