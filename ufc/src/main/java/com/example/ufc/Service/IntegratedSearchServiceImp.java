package com.example.ufc.Service;

import com.example.ufc.DTO.IntegratedSearchDTO;
import com.example.ufc.Entity.*;
import com.example.ufc.Repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class IntegratedSearchServiceImp implements IntegratedSearchService {

    @Autowired
    com.example.ufc.Repository.FighterRepository fighterRepository;

    @Autowired
    CommunityRepository communityRepository;

    @Autowired
    CommunityReplyRepository communityReplyRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    VoteBoardRepository voteBoardRepository;

    @Autowired
    IntegratedSearchLogRepository integratedSearchLogRepository;

    @Override
    @Transactional
    public IntegratedSearchDTO IntegratedSearch(String keyword, int page, int size) {

        // [핵심 추가] 검색어가 없거나 공백이면 DB 조회조차 하지 않고 즉시 리턴
        if (keyword == null || keyword.trim().isEmpty()) {
            return IntegratedSearchDTO.builder()
                    .keyword(keyword)
                    .fighters(Page.empty())
                    .post(Page.empty())
                    .reply(Page.empty())
                    .member(Page.empty())
                    .votes(Page.empty())
                    .popularFighters(integratedSearchLogRepository.findPopularFighters(LocalDateTime.now().minusDays(7)))
                    .build();
        }

        // 1. 기존 페이징 설정
        Pageable fighterPageable = PageRequest.of(page, size);
        Pageable postPageable = PageRequest.of(page, size);
        Pageable replyPageable = PageRequest.of(page, size);
        Pageable memberPageable = PageRequest.of(page, size);
        Pageable votePageable = PageRequest.of(page,size);
        // 2. 각 분야별 검색 수행 (기존 코드)
        Page<FighterEntity> fighters = fighterRepository.findFighter(keyword, fighterPageable);
        Page<CommunityEntity> post = communityRepository.findCommunityPost(keyword, keyword, postPageable); // 게시글
        Page<CommunityReplyEntity> reply = communityReplyRepository.findCommunityReply(keyword, replyPageable); // 답글, 댓글
        Page<MemberEntity> member = memberRepository.findUserId(keyword, keyword, memberPageable); // userId
        Page<VoteBoardEntity> votes = voteBoardRepository.findByKeyword(keyword,votePageable);
        // 🌟 [추가 로직 1] 검색 결과에 선수가 있다면 로그 저장
        if (!fighters.isEmpty()) {
            // 검색 결과 중 가장 연관도가 높은 첫 번째 선수 정보를 가져옴
            FighterEntity topMatch = fighters.getContent().get(0);

            // 검색 로그 생성 및 DB 저장
            IntegratedSearchLogEntity log = new IntegratedSearchLogEntity(topMatch);
            integratedSearchLogRepository.save(log);

//            integratedSearchLogRepository.save(
//                    new IntegratedSearchLogEntity(topMatch)
        }

        // 🌟 [추가 로직 2] 인기 선수 리스트 추출 (상위 5명, 최근 7일 기준)
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        List<FighterEntity> popularFighters = integratedSearchLogRepository.findPopularFighters(oneWeekAgo);

        return IntegratedSearchDTO.builder()
                .keyword(keyword)
                .fighters(fighters)
                .post(post)
                .reply(reply)
                .member(member)
                .votes(votes)
                .popularFighters(popularFighters)
                .build();
    }
}
