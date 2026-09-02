package com.example.ufc.DTO;

import com.example.ufc.Entity.FighterEntity;
import com.example.ufc.Entity.MemberEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NlpDTO {

    Long nlpNumber; // nlp 컬럼 번호

    String sourceType; // 어디 테이블에서 온 소스인지, COMMUNITY_TITLE / COMMUNITY_REPLY 같은 구분값

    Long sourceId; // 원본 테이블 컬럼 명

    String text; // 원문 텍스트

    FighterEntity fighterEntity; // 선수 테이블, fighterEntity에서 name만 Nlp로 들어감

    String sentiment; // 원문을 가져왔을 때 positive, negative 인지 구별하기 위한 컬럼, db에서 문자열 최대 길이 - positive까지 커버하력

    Integer weight; // 가중치

    Integer likeCount; // 좋아요 수 = 원문을 가져올 때 좋아요 수로 가중치를 다르게 적용

    MemberEntity memberEntity; // 원문의 작성자, community.id / reply.id, memberEntity에서

    LocalDateTime createdAt; // 원문이 생성된 시각, 이번 주 / 급상승 시각

    LocalDateTime analyzedAt; // nlp 분석 시각
}
