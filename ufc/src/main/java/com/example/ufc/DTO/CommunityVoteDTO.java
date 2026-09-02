package com.example.ufc.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommunityVoteDTO {

    Long communityContentNumber; // 게시글 번호
    Long replyNumber; // 답글 번호
    String id; // session
    int voteType; // 좋아요/싫어요 구분
}
