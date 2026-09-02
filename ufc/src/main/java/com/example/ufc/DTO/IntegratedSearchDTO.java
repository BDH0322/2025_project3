package com.example.ufc.DTO;

import com.example.ufc.Entity.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IntegratedSearchDTO {

    Page<FighterEntity> fighters;

    Page<CommunityEntity> post;

    Page<CommunityReplyEntity> reply;

    Page<MemberEntity> member;

    Page<VoteBoardEntity> votes;

    String keyword;

    List<FighterEntity> popularFighters;

}
