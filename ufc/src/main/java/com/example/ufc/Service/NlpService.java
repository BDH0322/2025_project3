package com.example.ufc.Service;

import java.time.LocalDateTime;
import java.util.List;

public interface NlpService {

    void communityPost(LocalDateTime time);
    void communityReply(LocalDateTime time);
    void SearchLog(LocalDateTime time);
    void voteBoard(LocalDateTime time);
    List<Object[]> getFighterRanking();
}
