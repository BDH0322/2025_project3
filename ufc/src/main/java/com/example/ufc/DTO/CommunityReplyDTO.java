package com.example.ufc.DTO;

import com.example.ufc.Entity.CommunityReplyEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommunityReplyDTO {

    private Long replyNumber; // 댓글 번호
    private Long communityContentNumber; // 게시글 번호
    private String id; //session
    private String replyContent; // 댓글 본문
    private String replyOriginalContent = null ; // 댓글 숨김 처리 시 이곳에 댓글 본문 저장
    private LocalDateTime replyTime; // 답글 작성 시간
    private LocalDateTime replyModifyTime; // 답글 수정 시간
    private int replyLike; // 답글 좋아요 갯수
    private int replyDisLike; // 답글 싫어요 갯수
    private int replyStatus; // 좋아요 싫어요 현황
    private Long parentReplyNumber; // 부모 댓글 번호 - 답글이 달릴 경로
    private int replyHidden; // 0:공개, 1:댓글 숨김처리, 2:댓글 아예 삭제

    private int admin; // 중요 01.11 추가, admin == 1 admin != 1, entity() 메서드는 그대로 (Entity에는 admin이 없으므로)

    @Builder.Default
    private List<CommunityReplyDTO> children = new ArrayList<>();

    // Entity로 변환 (댓글 저장/수정 시 사용)
    public CommunityReplyEntity entity() {
        return new CommunityReplyEntity(replyNumber, communityContentNumber, id, replyContent, replyOriginalContent, replyTime,
                replyModifyTime, replyLike, replyDisLike, parentReplyNumber, replyHidden);
    }

    // Entity로부터 DTO 생성 (댓글 목록 조회 시 사용)
    public static CommunityReplyDTO fromEntity(CommunityReplyEntity entity) {
        return CommunityReplyDTO.builder()
                .replyNumber(entity.getReplyNumber())
                .communityContentNumber(entity.getCommunityContentNumber())
                // id 하드코딩 제거: DB의 실제 ID를 사용합니다.
                .id(entity.getId())
                .replyContent(entity.getReplyContent())
                .replyOriginalContent(entity.getReplyOriginalContent())
                .replyTime(entity.getReplyTime())
                .replyModifyTime(entity.getReplyModifyTime())
                .replyLike(entity.getReplyLike())
                .replyDisLike(entity.getReplyDisLike())
                .parentReplyNumber(entity.getParentReplyNumber())
                .replyHidden(entity.getReplyHidden())
                .build();
    }
}
