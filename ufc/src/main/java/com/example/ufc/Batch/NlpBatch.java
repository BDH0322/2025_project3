package com.example.ufc.Batch;

import com.example.ufc.Service.NlpService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class NlpBatch {

    @Autowired
    NlpService nlpService;

    @Scheduled(fixedDelay = 1000*10) // 1000 × 60 × 30 = 1800초 = 30분
    public void run(){
        LocalDateTime time = LocalDateTime.now().minusMinutes(30);

        nlpService.communityPost(time);
        nlpService.communityReply(time);
        nlpService.SearchLog(time);
    }
}
