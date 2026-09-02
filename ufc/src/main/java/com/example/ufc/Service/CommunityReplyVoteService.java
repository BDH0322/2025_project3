package com.example.ufc.Service;

import jakarta.transaction.Transactional;

public interface CommunityReplyVoteService {

    @Transactional
    int vote(Long replyNumber, String id);

    void replylike(Long replyNumber, String id);

    void replydislike(Long replyNumber, String id);
}
