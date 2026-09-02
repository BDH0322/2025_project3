package com.example.ufc.Service;

import com.example.ufc.Entity.CommunityEntity;
import com.example.ufc.Entity.CommunityVoteEntity;
import com.example.ufc.Repository.CommunityRepository;
import com.example.ufc.Repository.CommunityVoteRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CommunityServiceImp implements CommunityService {

    @Autowired
    CommunityRepository communityRepository;

    @Autowired
    CommunityVoteRepository communityVoteRepository;

    @Value("${community.image.dir:C:/project/Data/community/image/}")
    String imageDir;

    @Override // 게시글 작성
    public void contentinsert(CommunityEntity centity) {
        centity.setCommunityWriteTime(LocalDateTime.now());
        centity.setCommunityWriteModifyTime(null);

        centity.setCommunityCommentCount(0);
        centity.setCommunityViewCount(0);
        centity.setCommunityLike(0);
        centity.setCommunityDisLike(0);

        communityRepository.save(centity);
    }


    @Override //게시글 수정
    public void contentmodify(CommunityEntity centity) {
        // 1. 원래 테이블에 들어있던 게시글 정보인데 modifyentity라고 해서 햇갈릴 수 있음 -> 어차피 새로 저장할 객체니까 modify라고 지었음
        CommunityEntity modifyentity = communityRepository.findById(centity.getCommunityContentNumber()).get();
//        .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));
        // a. DB에서 아이디로 게시글을 찾아서 "상자"에 담아옵니다.
        // b. 만약 상자가 비어있다면 에러를 던지세요!

        // 2. 새로운 이미지가 업로드된 경우
        if(centity.getCommunityImage() != null){ // 사용자가 새로운 사진을 올렸을 때
            String oldImageName = modifyentity.getCommunityImage();
            if(oldImageName != null){ // "글자 수가 0인 가짜 이름"까지 걸러내기 위한 꼼꼼한 검사.
                Path path = Paths.get(imageDir, oldImageName);
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } else{ // 사용자가 새로운 사진을 올리지 않았을 때
            centity.setCommunityImage(modifyentity.getCommunityImage());
        }

        centity.setCommunityWriteTime(modifyentity.getCommunityWriteTime());
        centity.setCommunityWriteModifyTime(LocalDateTime.now());
        centity.setCommunityCommentCount(modifyentity.getCommunityCommentCount());
        centity.setCommunityViewCount(modifyentity.getCommunityViewCount());
        centity.setCommunityLike(modifyentity.getCommunityLike());
        centity.setCommunityDisLike(modifyentity.getCommunityDisLike());

        communityRepository.save(centity);
    }

    @Override
    public CommunityEntity findcommunitypost(Long communityContentNumber) {
        return communityRepository.findById(communityContentNumber).orElse(null);
    }

    @Override // 게시글 삭제
    public void contentdelete(Long communityContentNumber) {
        Optional<CommunityEntity> postOptional = communityRepository.findById(communityContentNumber);

        postOptional.ifPresent(post -> {
            String imageFileName = post.getCommunityImage();

            if (imageFileName != null && !imageFileName.trim().isEmpty()){
                try {
                    Path filePath = Paths.get(imageDir, imageFileName);
                    if (Files.exists(filePath)) {
                        Files.delete(filePath);
                    }
                }catch (IOException e){
                    System.err.println("파일 삭제 중 오류 발생" + imageFileName + " / 오류: " + e.getMessage());
                }
            }
//            communityRepository.deleteById(communityContentNumber);
            communityRepository.delete(post);

        });
    }


    @Override
    public Page<CommunityEntity> findcommunity(Pageable pageable) {
        // 1. ROWNUM 쿼리에 필요한 offset과 pageSize 계산
        long offset = pageable.getOffset();
        int pageSize = pageable.getPageSize();

        List<CommunityEntity> content = communityRepository.PageAll(offset, pageSize);

        long total = communityRepository.countAllPosts();

        // 4. PageImpl을 사용하여 Page<T> 객체 수동 생성 및 반환
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<CommunityEntity> CommunitySearch(String searchType, String keyword, Pageable pageable) {
        if (keyword == null || keyword.isEmpty()) {
            return findcommunity(pageable); // 키워드 없으면 전체 목록 조회
        }

        // 1. 페이징 파라미터 계산
        long offset = pageable.getOffset();
        int pageSize = pageable.getPageSize();

        List<CommunityEntity> content;
        long total;

        // 2. 검색 타입에 따라 데이터 및 카운트 쿼리 실행
        switch (searchType) {
            case "communityTitle":
                content = communityRepository.findByCommunityTitleContaining(keyword, offset, pageSize);
                total = communityRepository.countByCommunityTitleContaining(keyword);
                break;

            case "communityContent":
                content = communityRepository.findByCommunityContentContaining(keyword, offset, pageSize);
                total = communityRepository.countByCommunityContentContaining(keyword);
                break;

            case "id":
                content = communityRepository.findByIdContaining(keyword, offset, pageSize);
                total = communityRepository.countByIdContaining(keyword);
                break;

            case "TitleOrContent":
                content = communityRepository.findByTitleOrContentContaining(keyword, offset, pageSize);
                total = communityRepository.countByTitleOrContentContaining(keyword);
                break;

            default:
                // 기본값: 제목으로 검색 (혹시 모를 오류 방지)
                content = communityRepository.findByCommunityTitleContaining(keyword, offset, pageSize);
                total = communityRepository.countByCommunityTitleContaining(keyword);
                break;
        }

        // 3. PageImpl을 사용하여 Page<T> 객체 수동 생성 및 반환
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    @Transactional
    public void viewCount(Long communityContentNumber) {
        communityRepository.viewCount(communityContentNumber);
    }

    @Override
    public int vote(Long communityContentNumber, String id) {
        return communityVoteRepository
                .VotedId(communityContentNumber,id)
                .map(CommunityVoteEntity::getVoteType)
                .orElse(0);
    }

    @Override
    @Transactional
    public void contentlike(Long communityContentNumber, String id) {
        //게시글 번호와 아이디로 투표 기록을 조회 - 좋아요
        Optional<CommunityVoteEntity> nowVote = communityVoteRepository.VotedId(communityContentNumber, id);
        CommunityEntity post = communityRepository.findById(communityContentNumber).orElseThrow();

        if(nowVote.isPresent()){
            CommunityVoteEntity vote = nowVote.get();

            if(vote.getVoteType() == 1){ // 1. 이미 좋아요 눌렀다면 좋아요 취소
                communityVoteRepository.delete(vote);
                post.setCommunityLike(post.getCommunityLike() - 1);
            } else{ // 2. 이미 싫어요 눌렀다면 싫어요 취소
                post.setCommunityDisLike(post.getCommunityDisLike() - 1);
                post.setCommunityLike(post.getCommunityLike() + 1);

                vote.setVoteType(1); // voteType == 1
                communityVoteRepository.save(vote);
            }
        } else{  // 3. 좋아요 눌렀다면 좋아요 증가
            CommunityVoteEntity newVote = new CommunityVoteEntity(communityContentNumber, id, 1);
            communityVoteRepository.save(newVote);
            post.setCommunityLike(post.getCommunityLike() + 1);
        }
        communityRepository.save(post);
    }

    @Override
    @Transactional
    public void contentdislike(Long communityContentNumber, String id) {
        Optional<CommunityVoteEntity> nowVote = communityVoteRepository.VotedId(communityContentNumber, id);
        CommunityEntity post = communityRepository.findById(communityContentNumber).orElseThrow();

        if (nowVote.isPresent()) {
            CommunityVoteEntity vote = nowVote.get();

            if (vote.getVoteType() == -1) { // 1. 이미 싫어요 눌렀다면 좋아요 취소
                communityVoteRepository.delete(vote);
                post.setCommunityDisLike(post.getCommunityDisLike() - 1);
            } else { // 2. 이미 싫어요 눌렀다면 싫어요 취소
                post.setCommunityLike(post.getCommunityLike() - 1);
                post.setCommunityDisLike(post.getCommunityDisLike() + 1);
                vote.setVoteType(-1); // voteType == -1
                communityVoteRepository.save(vote);
            }
        } else { // 3. 싫어요 눌렀다면 싫어요 증가
            CommunityVoteEntity newVote = new CommunityVoteEntity(communityContentNumber, id, -1);
            communityVoteRepository.save(newVote);
            post.setCommunityDisLike(post.getCommunityDisLike() + 1);
        }

        communityRepository.save(post);
    }

    @Override
    public Page<CommunityEntity> getCommunityListByCategoryAndSearch(Integer category, String searchType, String keyword, Pageable pageable) {
        // Controller에서 이 메서드를 호출했다면 category와 keyword는 유효하다고 가정합니다.

        // 1. 페이징 파라미터 계산
        long offset = pageable.getOffset();
        int pageSize = pageable.getPageSize();

        List<CommunityEntity> content;
        long total;

        // 2. 검색 타입에 따라 데이터 및 카운트 쿼리 실행 (기존 CommunitySearch 로직에 category 조건만 추가)
        switch (searchType) {
            case "communityTitle":
                content = communityRepository.findByCategoryAndCommunityTitleContaining(category, keyword, offset, pageSize);
                total = communityRepository.countByCategoryAndCommunityTitleContaining(category, keyword);
                break;

            case "communityContent":
                content = communityRepository.findByCategoryAndCommunityContentContaining(category, keyword, offset, pageSize);
                total = communityRepository.countByCategoryAndCommunityContentContaining(category, keyword);
                break;

            case "id":
                content = communityRepository.findByCategoryAndIdContaining(category, keyword, offset, pageSize);
                total = communityRepository.countByCategoryAndIdContaining(category, keyword);
                break;

            case "TitleOrContent":
                // 제목 또는 내용 검색
                content = communityRepository.findByCategoryAndTitleOrContentContaining(category, keyword, offset, pageSize);
                total = communityRepository.countByCategoryAndTitleOrContentContaining(category, keyword);
                break;

            default:
                // 기본값: 제목으로 검색 (혹시 모를 오류 방지)
                content = communityRepository.findByCategoryAndCommunityTitleContaining(category, keyword, offset, pageSize);
                total = communityRepository.countByCategoryAndCommunityTitleContaining(category, keyword);
                break;
        }

        // 3. PageImpl을 사용하여 Page<T> 객체 수동 생성 및 반환
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<CommunityEntity> getCommunityListByCategory(Integer category, Pageable pageable) {
        if (category == null) {
            // 혹시 Controller에서 category=null이 넘어오더라도 전체 목록을 반환하도록 처리
            return findcommunity(pageable);
        }

        // 1. 페이징 파라미터 계산 (기존 findcommunity 로직과 동일)
        long offset = pageable.getOffset();
        int pageSize = pageable.getPageSize();

        // 2. Repository 호출 (새로 만들 쿼리 호출)
        List<CommunityEntity> content = communityRepository.findByCommunityCategory(category, offset, pageSize);
        long total = communityRepository.countByCommunityCategory(category);

        // 3. PageImpl을 사용하여 Page<T> 객체 수동 생성 및 반환
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public void hidePost(Long communityContentNumber) {
        communityRepository.findById(communityContentNumber).ifPresent(post -> {
            post.setCommunityOriginalTitle(post.getCommunityTitle());
            post.setCommunityHidden(1);
            post.setCommunityTitle("✅ 관리자에 의해 숨김 처리 된 게시글 입니다.");
            communityRepository.save(post);
        });
    }

    @Override
    public void deletepost(Long communityContentNumber) {
        communityRepository.findById(communityContentNumber).ifPresent(post -> {
            String imageFileName = post.getCommunityImage();

            if(imageFileName != null && !imageFileName.trim().isEmpty()){
                File file = new File(imageDir + imageFileName);
                file.delete();
            }
            communityRepository.delete(post);
        });
    }

    @Override
    public void unhidePost(Long communityContentNumber) {
        communityRepository.findById(communityContentNumber).ifPresent(post -> {

            // 1. 게시글 숨김 상태 해제 (DB: communityHidden = 0)
            post.setCommunityHidden(0);

            if (post.getCommunityOriginalTitle() != null) {
                post.setCommunityTitle(post.getCommunityOriginalTitle());
                post.setCommunityOriginalTitle(null); // 사용 후 초기화
            }
            communityRepository.save(post);
        });

    }

    @Override
    public Page<CommunityEntity> getUserCommunity(String userId, Pageable pageable) {
        long offset = pageable.getOffset();
        int pageSize = pageable.getPageSize();

        // Repository 호출
        List<CommunityEntity> list = communityRepository.findUserCommunity(userId, offset, pageSize);
        long total = communityRepository.countUserCommunity(userId);

        return new PageImpl<>(list, pageable, total);
    }

    @Override
    public void banUser(String userId, int hours) {
        LocalDateTime endDate = LocalDateTime.now().plusDays(hours);
        communityRepository.updateBanEndDate(userId, endDate);
    }

    @Override
    public List<CommunityEntity> postlist() {
        return communityRepository.findpost(3);
    }

}
