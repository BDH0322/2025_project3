package com.example.ufc.DTO;

import com.example.ufc.Entity.CommunityEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommunityDTO {

    private Long communityContentNumber; // 커뮤니티 게시글 번호
    private String id; // 사용자 id
    private int communityCategory; // 커뮤니티 게시판 종류 (체급 별)

    private String communityTitle; // 게시글 제목
    private String communityOriginalTitle; // 게시글 숨김 취소 했을 때 넣어줄 원래 제목 저장될 컬럼
    private String communityContent; // 게시글 본문
    private String communityImage; //게시글 이미지
    private int communityViewCount; // 게시글 조회수

    private int communityLike; // 게시글 좋아요
    private int communityDisLike; // 게시글 싫어요
//    private int voteType; // 게시글 좋아요, 싫어요 구분

    private int communityCommentCount; // 게시글 댓글 수
    private LocalDateTime communityWriteTime; // 게시글 최초 작성 시간
    private LocalDateTime communityWriteModifyTime; // 게시글 수정 시간

    private int admin; // 권한 = 0 : 어드민,  1 : 일반 유저
    private int communityHidden = 0; // 0:공개, 1:숨김(블라인드)
    private int communityPin = 0; // 0:일반, 1:상단 고정
    private int communityIsNotice = 0; // 0:일반, 1:공지글


    public CommunityEntity entity() {
        return new CommunityEntity(communityContentNumber, id, communityCategory ,communityTitle, communityOriginalTitle, communityContent, communityImage, communityViewCount, communityLike, communityDisLike, communityCommentCount, communityWriteTime, communityWriteModifyTime, admin, communityHidden, communityPin, communityIsNotice);
    }

}