package com.example.ufc.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "nlp")
@SequenceGenerator(name = "nlp",
sequenceName = "nlp_seq",
initialValue = 1,
allocationSize = 1)
public class NlpEntity {

//    이 테이블(NLP 테이블)을 왜 만들었는가
//    커뮤니티 글, 댓글, 검색 기록처럼 사람의 행동과 발화가 흩어져 있는 데이터를 한 곳에 모아 “이번 주 어떤 파이터가 실제로 인기 있는가”를 수치로 판단하기 위해

//    그럼 nlp란? = 자연어 처리(Natural Language Processing, NLP)의 영단어임
//    사람 말투 그대로의 텍스트를 데이터로 쓰는 것 - 뉴스기사 커뮤니티 댓글, 검색어, X 인스타 글 등을 한데 모아 특정 키워드를 축출해 긍정과 부정 따위에 기준으로 나누어 분류
//    이렇게 분류된 데이터들로 통계를 만들어 원하는 기능을 만드는 것이 목표임

//    NLPNUMBER는 자연어 처리를 위해 들어오는 데이터들의 시퀀스 넘버이다.

//    소스 타입은 들어온 데이터가 어느 테이블에서 온 데이터인지 테이블명을 컬럼에 저장하며,
//    소스 id는 들어온 데이터의 원래 테이블 pk를 컬럼에 저장하는 역할을 나눠가지고 text는 이 데이터들의 원문을 컬럼에 저장한다!

//    이렇게 3개의 컬럼들은 들어온 데이터의 기본적인 위치와 정보를 저장하는 역할을 함
//    다음은 이 자연어 처리 테이블이 수집하는 *******데이터의 범위******* 를 알아본다.

//    *******데이터의 범위*******
//        1. 메인 통합 검색 기록
//        2. 커뮤니티 게시글 제목, 댓글

//    이렇게 가져온다.
//    여기서 중요한 점은 가져온 데이터들이 같은 사용자에 의해 중복될 수 있다는 점이다.
//
//    이 점을 방지하기 위해 두개의 컬럼을 추가함

//        1. memberEntity 사용자의 ID만을 저장한다
//        2. fighterEntity 선수의 NAME만을 저장한다.

//    동일 사용자의 반복적인 언급을 식별하거나 가중치를 보정하기 위한 기준 정보로 사용된다.
//    이제 NLP처리를 위해 기준(sentiment)을 설정 -> sentiment = 긍정(positive), 부정(negative)_2가지 기준을 설정 (필요 시 neutral 확장가능)

//    이런 기준(sentiment)들과 변수(likeCount, 시간, sourceType 등)들을 한데 모아 weight(가중치)라는 수식이 만들어진다.
//    필요한 이유는 같은 데이터라도 좋아요 수 라거나, 작성된 시간, 작성된 게시글의 성격이 다르면 글자가 가지는 밀도가 달라지기 때문임

//    마지막으로 createdAt, analyzedAt는 데이터가 들어오는 시간과 데이터가 nlp로 쓰임되는 시간을 저장하는 컬럼이다.
//    왜냐하면 createdAt로 이번 주 또는 급상승 같은 레벨을 적용할 수 있기 때문임
//    왜냐하면 nlp데이터로 쓰여지는 시간을 알아두는건 좋은 관습이기 때문임


    @Id
    @Column // nlp 컬럼 번호
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "nlp")
    Long nlpNumber;

    @Column // 어디 테이블에서 온 소스인지, COMMUNITY_TITLE / COMMUNITY_REPLY 같은 구분값
    String sourceType;

    @Column // 원본 테이블 컬럼 명
    Long sourceId;

    @Lob
    @Column // 원문 텍스트
    String text;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fighter_name", referencedColumnName = "NAME") // 선수 테이블, fighterEntity에서 name만 Nlp로 들어감
    FighterEntity fighterEntity;

    @Column(length = 10) // 원문을 가져왔을 때 positive, negative 인지 구별하기 위한 컬럼, db에서 문자열 최대 길이 - positive까지 커버하력
    String sentiment;

    @Column(name = "sentiment_score")
    Double sentimentScore;

    @Column // 가중치
    Integer weight;

    @Column // 좋아요 수 = 원문을 가져올 때 좋아요 수로 가중치를 다르게 적용
    Integer likeCount;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "member_id") // 원문의 작성자, community.id / reply.id, memberEntity에서
    MemberEntity memberEntity;

    @Column // 원문이 생성된 시각, 이번 주 / 급상승 시각
    LocalDateTime createdAt;

    @Column // nlp 분석 시각
    LocalDateTime analyzedAt;

}
