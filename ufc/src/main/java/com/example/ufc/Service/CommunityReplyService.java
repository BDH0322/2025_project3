package com.example.ufc.Service;

import com.example.ufc.DTO.CommunityReplyDTO;
import com.example.ufc.Entity.CommunityReplyEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CommunityReplyService {
    void replysave(CommunityReplyEntity communityReplyEntity);

    void replydelete(Long replyNumber);

    void replymodify(Long replyNumber, String replyContent);

    List<CommunityReplyDTO> findreply(Long communityContentNumber, String id);

    void replyreplysave(CommunityReplyEntity replyreplyentity);

    void hideReply(Long replyNumber);

    void unhideReply(Long replyNumber);

    void deleteReplyHard(Long replyNumber);

    void insertReplyCount(Long communityContentNumber, int i);
}
