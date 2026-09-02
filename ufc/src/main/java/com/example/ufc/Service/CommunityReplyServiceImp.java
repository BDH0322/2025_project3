package com.example.ufc.Service;

import com.example.ufc.DTO.CommunityReplyDTO;
import com.example.ufc.Entity.CommunityEntity;
import com.example.ufc.Entity.CommunityReplyEntity;
import com.example.ufc.Repository.CommunityReplyRepository;
import com.example.ufc.Repository.CommunityReplyVoteRepository;
import com.example.ufc.Repository.CommunityRepository;
import com.example.ufc.Repository.MemberRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommunityReplyServiceImp implements CommunityReplyService {

    @Autowired
    CommunityReplyRepository communityReplyRepository;

    @Autowired
    CommunityReplyVoteRepository communityReplyVoteRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    CommunityRepository communityRepository;

    @Override
    public void replysave(CommunityReplyEntity communityReplyEntity) {
        communityReplyEntity.setReplyTime(LocalDateTime.now());
        communityReplyEntity.setReplyModifyTime(null);
        communityReplyEntity.setParentReplyNumber(null); //0L 보다 null 권장
        communityReplyEntity.setReplyLike(0);
        communityReplyEntity.setReplyDisLike(0);
        communityReplyEntity.setReplyHidden(0);
        communityReplyEntity.setReplyOriginalContent(null);

        communityReplyRepository.save(communityReplyEntity);
        insertReplyCount(communityReplyEntity.getCommunityContentNumber(), 1);
    }

    @Override
    public void replydelete(Long replyNumber) {
        CommunityReplyEntity reply = communityReplyRepository.findById(replyNumber).orElse(null);
        if (reply != null){
            insertReplyCount(reply.getCommunityContentNumber(), -1);
        }
        communityReplyRepository.deleteById(replyNumber);

    }

    @Override
    @Transactional // db에 변동을 주니까
    public void insertReplyCount(Long communityContentNumber, int i) {
        CommunityEntity post = communityRepository.findById(communityContentNumber).orElse(null);
        if (post != null){
            post.setCommunityCommentCount(post.getCommunityCommentCount() + i);
            communityRepository.save(post);
        }
    }

    @Override
    public void replymodify(Long replyNumber, String replyContent) {
        communityReplyRepository.replyModify(replyNumber, replyContent, LocalDateTime.now());
    }

    @Override
    public List<CommunityReplyDTO> findreply(Long communityContentNumber, String id) {
        // 1. 해당 게시글의 모든 댓글 엔티티 조회
        List<CommunityReplyEntity> replies = communityReplyRepository.findByCommunityContentNumber(communityContentNumber);

        // 2. DTO 변환 및 Map 생성
        List<CommunityReplyDTO> replyDTOs = replies.stream()
                // 💡 수정 완료: DTO의 fromEntity 정적 메서드를 사용합니다.
                .map(entity -> {
                    CommunityReplyDTO dto = CommunityReplyDTO.fromEntity(entity);

                    // ⭐ 실시간 권한 조회: 작성자의 ID로 Member 테이블을 조회해 admin 값을 세팅
                    memberRepository.findById(entity.getId()).ifPresent(member -> {
                        dto.setAdmin(member.getAdmin());
                    });

                    return dto;
                })
                .collect(Collectors.toList());

        // Map을 사용하여 ID(replyNumber)로 DTO를 빠르게 찾을 수 있도록 준비
        Map<Long, CommunityReplyDTO> dtoMap = replyDTOs.stream()
                .collect(Collectors.toMap(CommunityReplyDTO::getReplyNumber, dto -> dto));

        List<CommunityReplyDTO> rootReplies = new ArrayList<>();

        // 3. 부모-자식 구조화 (계층 구조 생성)
        for (CommunityReplyDTO dto : replyDTOs) {
            // 부모 댓글 번호가 null이거나 0이면 최상위(루트) 댓글입니다.
            if (dto.getParentReplyNumber() == null || dto.getParentReplyNumber() == 0) {
                rootReplies.add(dto);
            } else {
                // 답글인 경우, 부모 댓글을 찾습니다.
                CommunityReplyDTO parent = dtoMap.get(dto.getParentReplyNumber());
                if (parent != null) {
                    // 부모 DTO의 children 리스트에 현재 답글을 추가합니다.
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(dto);
                }
            }
        }

        return rootReplies;
    }

    @Override
    public void replyreplysave(CommunityReplyEntity replyreplyentity) {
        replyreplyentity.setReplyTime(LocalDateTime.now());
        replyreplyentity.setReplyModifyTime(null);
        replyreplyentity.setReplyLike(0);
        replyreplyentity.setReplyDisLike(0);
        replyreplyentity.setReplyHidden(0); // 0: 공개 상태로 초기화
        replyreplyentity.setReplyOriginalContent(null);

        communityReplyRepository.save(replyreplyentity);
        insertReplyCount(replyreplyentity.getCommunityContentNumber(), 1);
    }

    @Override
    public void hideReply(Long replyNumber) {
        String HIDDEN_MESSAGE = "관리자에 의해 숨김 처리된 댓글입니다.";
        communityReplyRepository.hideReplyAdmin(replyNumber, HIDDEN_MESSAGE);

    }

    @Override
    public void unhideReply(Long replyNumber) {
        communityReplyRepository.unhideReplyAdmin(replyNumber);
    }

    @Override
    public void deleteReplyHard(Long replyNumber) {
        communityReplyRepository.deleteById(replyNumber);
    }

}
