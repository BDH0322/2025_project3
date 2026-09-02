package com.example.ufc.Service;

import com.example.ufc.Entity.*;
import com.example.ufc.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NlpServiceImp implements NlpService{

    @Autowired
    VoteBoardRepository voteBoardRepository;
    @Autowired
    CommunityRepository communityRepository;

    @Autowired
    CommunityReplyRepository communityReplyRepository;

    @Autowired
    IntegratedSearchLogRepository integratedSearchLogRepository;

    @Autowired
    com.example.ufc.Repository.FighterRepository fighterRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    NlpRepository nlpRepository;

    @Override
    public void communityPost(LocalDateTime time) {
            // 1. 지정된 시간 이후의 게시글과 전체 선수 목록을 가져옴
            List<CommunityEntity> posts = communityRepository.CommunityWriteTimeAfter(time);
            List<FighterEntity> allFighters = fighterRepository.findAll();


            for (CommunityEntity post : posts) {
                for (FighterEntity fighter : allFighters) {
                    String fighterName = fighter.getName();

                    // --- [A] 게시글 제목 분석 ---
                    if (post.getCommunityTitle() != null && post.getCommunityTitle().contains(fighterName)) {
                        // 중복 체크 (출처, 아이디, 선수 셋 다 맞아야 중복)
                        if (nlpRepository.existsRowWithFighter("COMMUNITY_TITLE", post.getCommunityContentNumber(), fighter.getName()) == null) {
                            NlpEntity nlp = new NlpEntity();
                            nlp.setSourceType("COMMUNITY_TITLE");
                            nlp.setSourceId(post.getCommunityContentNumber());
                            nlp.setText(post.getCommunityTitle());
                            nlp.setFighterEntity(fighter);
                            nlp.setMemberEntity(memberRepository.findById(post.getId()).orElse(null));
                            nlp.setLikeCount(post.getCommunityLike());
                            nlp.setWeight(2); // 제목 가중치 2
                            nlp.setCreatedAt(post.getCommunityWriteTime());
                            nlp.setAnalyzedAt(LocalDateTime.now());
                            nlpRepository.save(nlp);
                        }
                    }

                    // --- [B] 게시글 본문 분석 ---
                    if (post.getCommunityContent() != null && post.getCommunityContent().contains(fighterName)) {
                        if (nlpRepository.existsRowWithFighter("COMMUNITY_CONTENT", post.getCommunityContentNumber(), fighter.getName()) == null) {
                            NlpEntity nlp = new NlpEntity();
                            nlp.setSourceType("COMMUNITY_CONTENT");
                            nlp.setSourceId(post.getCommunityContentNumber());
                            nlp.setText(post.getCommunityContent()); // 본문 원문 전체 저장
                            nlp.setFighterEntity(fighter);
                            nlp.setMemberEntity(memberRepository.findById(post.getId()).orElse(null));
                            nlp.setLikeCount(post.getCommunityLike());
                            nlp.setWeight(1); // 본문 가중치 1
                            nlp.setCreatedAt(post.getCommunityWriteTime());
                            nlp.setAnalyzedAt(LocalDateTime.now());
                            nlpRepository.save(nlp);
                        }
                    }
                }
            }
    }

    @Override
    public void communityReply(LocalDateTime time) {
        List<CommunityReplyEntity> replies = communityReplyRepository.CommunityReplyWriteTimeAfter(time);
        List<FighterEntity> allFighters = fighterRepository.findAll();

        for (CommunityReplyEntity reply : replies) {
            for (FighterEntity fighter : allFighters) {
                if (reply.getReplyContent() != null && reply.getReplyContent().contains(fighter.getName())) {
                    if (nlpRepository.existsRowWithFighter("COMMUNITY_REPLY", reply.getReplyNumber(), fighter.getName()) == null) {
                        NlpEntity nlp = new NlpEntity();
                        nlp.setSourceType("COMMUNITY_REPLY");
                        nlp.setSourceId(reply.getReplyNumber());
                        nlp.setText(reply.getReplyContent());
                        nlp.setFighterEntity(fighter);
                        nlp.setMemberEntity(memberRepository.findById(reply.getId()).orElse(null));
                        nlp.setLikeCount(reply.getReplyLike());
                        nlp.setWeight(1);
                        nlp.setCreatedAt(reply.getReplyTime());
                        nlp.setAnalyzedAt(LocalDateTime.now());
                        nlpRepository.save(nlp);
                    }
                }
            }
        }
    }

    @Override
    public void SearchLog(LocalDateTime time) {
        List<IntegratedSearchLogEntity> logs = integratedSearchLogRepository.SearchTime(time);

        for (IntegratedSearchLogEntity log : logs) {
            // 검색 로그는 이미 선수 객체를 가지고 있으므로 텍스트 중복 체크만 진행
            if (nlpRepository.existsRow("SEARCH_LOG", log.getLog()) == null) {
                NlpEntity nlp = new NlpEntity();
                nlp.setSourceType("SEARCH_LOG");
                nlp.setSourceId(log.getLog());
                nlp.setText(log.getFighterEntity().getName());
                nlp.setFighterEntity(log.getFighterEntity());
                nlp.setWeight(3); // 검색 로그 가중치 3
                nlp.setCreatedAt(log.getSearchTime());
                nlp.setAnalyzedAt(LocalDateTime.now());
                nlpRepository.save(nlp);
            }
        }
    }

    @Override
    public void voteBoard(LocalDateTime time){
        //1.특정 시간 이후 생성된 투표 게시글 조회
        List<VoteBoardEntity> votes = voteBoardRepository.findByCreateAtAfter(time);
        List<FighterEntity> allFighters = fighterRepository.findAll();


        for(VoteBoardEntity vote : votes){
            for(FighterEntity fighter : allFighters) {
                String fName = fighter.getName();


                //제목이나 내용에 선수 이름이 포함되어 있는지 확인
                boolean inTitle = vote.getTitle() != null && vote.getTitle().contains(fName);
                boolean inContent = vote.getContent() != null && vote.getContent().contains(fName);

                if(inTitle || inContent){
                    //중복 저장 방지 체크
                    if(nlpRepository.existsRowWithFighter("VOTE_BOARD", vote.getId(), fName) == null) {
                        NlpEntity nlp = new NlpEntity();
                        nlp.setSourceType("VOTE_BOARD");
                        nlp.setSourceId(vote.getId());
                        nlp.setText(vote.getTitle()); // 대표 텍스트로 제목 저장
                        nlp.setFighterEntity(fighter);

                        //가중치 설정: 투표글은 인기도에 큰 영향을 주므로 3점 부여
                        nlp.setWeight(3);

                        //투표글은 총 투표수를 LikeCount처럼 활용가능
                        nlp.setLikeCount(vote.getFighter1Votes() + vote.getFighter2Votes());

                        nlp.setCreatedAt(vote.getCreateAt());
                        nlp.setAnalyzedAt(LocalDateTime.now());
                        nlpRepository.save(nlp);
                    }



                }
            }
        }


    }


    public List<Object[]> getFighterRanking() {
        return nlpRepository.findFighterRank();
    }
}
