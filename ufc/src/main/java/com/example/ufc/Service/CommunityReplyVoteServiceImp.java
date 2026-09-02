package com.example.ufc.Service;

import com.example.ufc.Entity.CommunityReplyEntity;
import com.example.ufc.Entity.CommunityReplyVoteEntity;
import com.example.ufc.Repository.CommunityReplyRepository;
import com.example.ufc.Repository.CommunityReplyVoteRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CommunityReplyVoteServiceImp implements CommunityReplyVoteService{

    @Autowired
    CommunityReplyVoteRepository communityReplyVoteRepository;

    @Autowired
    CommunityReplyRepository communityReplyRepository;

    @Transactional
    @Override
    public int vote(Long replyNumber, String id) {
        return communityReplyVoteRepository.VotedId(replyNumber,id).map(CommunityReplyVoteEntity::getVoteStatus).orElse(0);
    }

    @Override
    @Transactional
    public void replylike(Long replyNumber, String id) {
        // 1. 해당 댓글의 기존 투표 기록 조회
        Optional<CommunityReplyVoteEntity> nowVote = communityReplyVoteRepository.VotedId(replyNumber, id);

        // 2. 댓글 엔티티 조회
        CommunityReplyEntity reply = communityReplyRepository.findById(replyNumber)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        if (nowVote.isPresent()) {
            CommunityReplyVoteEntity vote = nowVote.get();

            if (vote.getVoteStatus() == 1) {
                // 1. 이미 좋아요를 눌렀다면 -> 좋아요 취소 (투표 기록 삭제)
                communityReplyVoteRepository.delete(vote);
                reply.setReplyLike(reply.getReplyLike() - 1);

            } else {
                // 2. 이미 싫어요(-1)를 눌렀다면 -> 좋아요로 변경 (싫어요 -1, 좋아요 +1)
                reply.setReplyDisLike(reply.getReplyDisLike() - 1); // 기존 싫어요 카운트 감소
                reply.setReplyLike(reply.getReplyLike() + 1);       // 새 좋아요 카운트 증가

                vote.setVoteStatus(1); // 투표 상태를 좋아요(1)로 변경
                communityReplyVoteRepository.save(vote);
            }
        } else {
            // 3. 투표 기록이 없다면 -> 새로운 좋아요 투표
            CommunityReplyVoteEntity newVote = new CommunityReplyVoteEntity(replyNumber, id, 1); // 1: 좋아요
            communityReplyVoteRepository.save(newVote);
            reply.setReplyLike(reply.getReplyLike() + 1);
        }

        // 4. 변경된 댓글 카운트를 DB에 반영
        communityReplyRepository.save(reply);
    }

    @Override
    @Transactional
    public void replydislike(Long replyNumber, String id) {
        // 1. 해당 댓글의 기존 투표 기록 조회
        Optional<CommunityReplyVoteEntity> nowVote = communityReplyVoteRepository.VotedId(replyNumber, id);

        // 2. 댓글 엔티티 조회
        CommunityReplyEntity reply = communityReplyRepository.findById(replyNumber)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        if (nowVote.isPresent()) {
            CommunityReplyVoteEntity vote = nowVote.get();

            if (vote.getVoteStatus() == -1) {
                // 1. 이미 싫어요를 눌렀다면 -> 싫어요 취소 (투표 기록 삭제)
                communityReplyVoteRepository.delete(vote);
                reply.setReplyDisLike(reply.getReplyDisLike() - 1);

            } else {
                // 2. 이미 좋아요(1)를 눌렀다면 -> 싫어요로 변경 (좋아요 -1, 싫어요 +1)
                reply.setReplyLike(reply.getReplyLike() - 1);       // 기존 좋아요 카운트 감소
                reply.setReplyDisLike(reply.getReplyDisLike() + 1); // 새 싫어요 카운트 증가

                vote.setVoteStatus(-1); // 투표 상태를 싫어요(-1)로 변경
                communityReplyVoteRepository.save(vote);
            }
        } else {
            // 3. 투표 기록이 없다면 -> 새로운 싫어요 투표
            CommunityReplyVoteEntity newVote = new CommunityReplyVoteEntity(replyNumber, id, -1); // -1: 싫어요
            communityReplyVoteRepository.save(newVote);
            reply.setReplyDisLike(reply.getReplyDisLike() + 1);
        }

        // 4. 변경된 댓글 카운트를 DB에 반영
        communityReplyRepository.save(reply);
    }


}
