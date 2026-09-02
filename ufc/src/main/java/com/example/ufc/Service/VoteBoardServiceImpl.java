package com.example.ufc.Service;


import com.example.ufc.DTO.VoteBoardDTO;
import com.example.ufc.Entity.VoteBoardEntity;
import com.example.ufc.Repository.TotalPredictionRepository;
import com.example.ufc.Repository.VoteBoardRepository;
import com.example.ufc.Repository.VoteHistoryRepository;
import io.micrometer.observation.annotation.Observed;
import org.springframework.data.domain.*;
import org.springframework.security.core.parameters.P;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoteBoardServiceImpl implements VoteBoardService{
    private final VoteBoardRepository voteBoardRepository;
    private final VoteHistoryRepository voteHistoryRepository;
    private final TotalPredictionService totalPredictionService;
    private final TotalPredictionRepository totalPredictionRepository;

    private final Map<String,Long> startRange = Map.of(
      "Featherweight",1000L,
      "Lightweight", 2000L,
      "Welterweight", 3000L,
      "Middleweight", 4000L,
      "Light Heavyweight",5000L,
      "Heavyweight",6000L
    );

    /*
     * [수정 부분]
     * ORA-00933 에러 방지를 위한 수동 페이징 처리 로직
     */
    @Override
    public Page<VoteBoardDTO> getVoteList(int page,String keyword) {
        int pageSize = 10; // 한 페이지당 보여줄 게시글 수

        // 1. Native Query를 사용하여 전체 데이터를 리스트로 가져옴 (fetch first 방지)
        List<VoteBoardEntity> allEntities = voteBoardRepository.findAllNative();



        // 3. 현재 페이지에 맞는 데이터를 Stream으로 슬라이싱 (메모리 페이징)
        List<VoteBoardDTO> filterdList;
        if(keyword != null && !keyword.trim().isEmpty()) {
         filterdList=allEntities.stream()
                    .filter(e-> (e.getTitle() != null && e.getTitle().contains(keyword))||
                            (e.getFighter1Name() !=null && e.getFighter1Name().contains(keyword))||
                            (e.getFighter2Name() !=null && e.getFighter2Name().contains(keyword)))
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        }
        else{
            //검색어가 없으면 전체를 dto로 전환
            filterdList = allEntities.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        }
        //3.필터링된 결과(검색어 포함 혹은 전체)의 개수 파악
        int total = filterdList.size();

        //4.슬라이싱(메모리 페이징)
        List<VoteBoardDTO> pagedList = filterdList.stream()
                .skip((long)page*pageSize)
                .limit(pageSize)
                .collect(Collectors.toUnmodifiableList());

        // 5. PageImpl 객체를 생성하여 반환 (컨트롤러/HTML은 기존 코드 유지 가능)
        return new PageImpl<>(pagedList, PageRequest.of(page, pageSize), total);
    }

    @Override
    @Transactional
    public void createVote(VoteBoardDTO dto, MultipartFile voteImage){
        VoteBoardEntity entity = new VoteBoardEntity();
        //1. 체급별 boardNum 자동 할당 로직
        Long start = startRange.getOrDefault(dto.getWeightClass(),9000L);
        Long end = start +1000L;

        Long maxNum = voteBoardRepository.findMaxBoardNumByRange(start,end);
        entity.setBoardNum(maxNum == null ? start : maxNum +1); // 1000또는 max+1

        //[이미지 처리 로직]
        if(voteImage != null && !voteImage.isEmpty()){
            try{
                //1.프로젝트 내 static폴더의 절대 경로 찾기
                //주의: 실무에서는 프로젝트 외부경로르 권장함
                String absolutePath = new File("src/main/resources/static/images/voteUpload/").getAbsolutePath();
                //2. 파일명 중복 방지(uuid 사용)
                String fileName = UUID.randomUUID().toString() + "_" + voteImage.getOriginalFilename();
                File saveFile = new File(absolutePath,fileName);
                //3.폴더가 없으면 생성
                if(!saveFile.getParentFile().exists()) {
                    saveFile.getParentFile().mkdirs();
                }
                //4.물리적 저장
                voteImage.transferTo(saveFile);

                //5.db에는 웹에서 접근 가능한 경로만 저장
                //static폴더는 서버 실행 시 루트(/)로 잡히므로 /images/... 로 저장
                entity.setVoteImage("/images/voteUpload/" + fileName);
            } catch (IOException e) {
                throw new RuntimeException("이미지 저장 중 오류 발생",e);
            }


        }


            //2.DTO -> Entity 매핑
            entity.setFightNum(dto.getFightNum());
            entity.setTitle(dto.getTitle());
            entity.setContent(dto.getContent());
            entity.setWeightClass(dto.getWeightClass());
            entity.setFighter1Name(dto.getFighter1Name());
            entity.setFighter2Name(dto.getFighter2Name());

        voteBoardRepository.save(entity);
        // 2. [핵심] 저장된 게시글 정보를 넘겨서 초기 AI 점수를 생성합니다.
        // 이 메서드가 실행되면서 TotalPrediction 테이블에 한 행(row)이 생깁니다.
        totalPredictionService.saveInitialAiScore(entity);

    }
    @Override
    @Transactional
    public void updateVote(Long boardNum, VoteBoardDTO dto, MultipartFile voteImage){
        //1.기존 게시글 조회
        VoteBoardEntity vote = voteBoardRepository.findByBoardNum(boardNum)
                .orElseThrow(()-> new IllegalArgumentException("해당 게시글이 존재하지 않습니다. ID:" + boardNum));

        //2. 기본 정보 업데이트
        vote.setTitle(dto.getTitle());
        vote.setContent(dto.getContent());
        //3.선수 이름 수정 제한 로직
        if(!vote.isClosed()) {
            //마감되지 않았을 떄만 선수이름 변경 가능
            vote.setFighter1Name(dto.getFighter1Name());
            vote.setFighter2Name(dto.getFighter2Name());
        }
        //3.이미지 수정처리

        if(voteImage != null && !voteImage.isEmpty()){
            //기존 파일이 있다면 삭제 (서버 용량 관리)
            if(vote.getVoteImage() != null){
                deletePhysicalFile(vote.getVoteImage());

            }

            //새 파일 저장 (createvote 로직 재사용)
            try{
                String ap = new File("src/main/resources/static/images/voteUpload/").getAbsolutePath();
                String fileName = UUID.randomUUID().toString()+"_"+voteImage.getOriginalFilename();
                File saveFile = new File(ap,fileName);

                voteImage.transferTo(saveFile);
                vote.setVoteImage("/images/voteUpload/"+fileName);
            } catch (IOException e){
                throw new RuntimeException("새 이미지 저장 중 오류 발생");

            }

        }
    }

    @Override
    @Transactional(readOnly = true) //spring framework에서 찾아야 함
    public List<VoteBoardDTO> getAllVotes() {
        return voteBoardRepository.findAllByOrderByBoardNumDesc().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public VoteBoardDTO getVoteByBoardNum(Long boardNum) {
        VoteBoardEntity entity = voteBoardRepository.findByBoardNum(boardNum)
                .orElseThrow(() -> new RuntimeException("해당 번호의 투표 게시판을 찾을 수 없습니다."));
        return convertToDto(entity);
    }

    /*
    * 아이디당 1회 제한을 둔 투표 로직
    * */
    @Override
    @Transactional
    public void addVoteWithCheck(Long boardNum, int fighterNum, String userId) {
        // 1. 중복 투표 체크
        if (voteHistoryRepository.existsByBoardNumAndUserId(boardNum, userId)) {
            throw new IllegalStateException("이미 이 경기에 투표하셨습니다.");
        }

        // 2. 게시글 존재 여부 및 마감 확인
        VoteBoardEntity entity = voteBoardRepository.findByBoardNum(boardNum)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다"));

        if (entity.isClosed()) {
            throw new RuntimeException("이미 마감된 투표입니다.");
        }

        // 3. 투표 가산
        if (fighterNum == 1) {
            entity.setFighter1Votes(entity.getFighter1Votes() + 1);
        } else if (fighterNum == 2) {
            entity.setFighter2Votes(entity.getFighter2Votes() + 1);
        }

        // 4. 투표 기록 저장 (생성자 에러 방지를 위해 Setter 방식 사용)
        com.example.ufc.Entity.VoteHistoryEntity history = new com.example.ufc.Entity.VoteHistoryEntity();
        history.setBoardNum(boardNum);
        history.setUserId(userId);
        voteHistoryRepository.save(history);
    }


    @Override
    @Transactional
    public void closeVote(Long boardNum){
        VoteBoardEntity entity = voteBoardRepository.findByBoardNum(boardNum)
                .orElseThrow(()-> new RuntimeException("게시글을 찾을 수 없습니다."));
        entity.setClosed(true);

    }

    private VoteBoardDTO convertToDto(VoteBoardEntity entity){
        VoteBoardDTO dto = new VoteBoardDTO();
        dto.setBoardNum(entity.getBoardNum());
        dto.setFightNum(entity.getFightNum());
        dto.setTitle(entity.getTitle());
        dto.setContent(entity.getContent());
        dto.setFighter1Name(entity.getFighter1Name());
        dto.setFighter2Name(entity.getFighter2Name());
        dto.setFighter1Votes(entity.getFighter1Votes());
        dto.setFighter2Votes(entity.getFighter2Votes());
        dto.setClosed(entity.isClosed());
        dto.setCreatedAt(entity.getCreateAt());
        dto.setVoteImagePath(entity.getVoteImage());
        dto.setWeightClass(entity.getWeightClass()); // 이 줄이 반드시 있어야 합니다!
        return dto;
    }
    @Override
    @Transactional(readOnly = true)
    public boolean checkIfVoted(Long boardNum, String userId) {
      if(userId == null) return false;
      //db에서 해당 유저가 특정 게시글에 투표한 기록이 있는지 확인
        return voteHistoryRepository.existsByBoardNumAndUserId(boardNum,userId);
    }

    @Override
    @Transactional
    public void deleteVote(Long boardNum){
        //1. 삭제할 게시글 조회
        VoteBoardEntity vote = voteBoardRepository.findByBoardNum(boardNum)
                .orElseThrow(()-> new IllegalArgumentException("삭제할 게시글이 없습니다."));

        //2. 물리 파일 삭제 (서버 용량 관리)
        if(vote.getVoteImage() != null){
            deletePhysicalFile(vote.getVoteImage());
        }
        // 3. 연관 데이터 삭제 (순서 중요)
        // [수정] 투표 히스토리 삭제
        voteHistoryRepository.deleteByBoardNumNative(boardNum);

        //ai예측 데이터 삭제
        //totalPredictionRepository.deleteById(boardNum);
        // [핵심 수정] AI 예측 데이터 삭제 (PK가 아닌 연관 엔티티로 찾아서 삭제)
        totalPredictionRepository.findByVoteBoard(vote)
                //.ifPresent(totalPredictionRepository::delete);
                .ifPresent(prediction -> totalPredictionRepository.delete(prediction));

        //게시글(부모) 삭제
        voteBoardRepository.delete(vote);
    }


    private void deletePhysicalFile(String filePath){
       if(filePath == null || filePath.isEmpty()) return;

       try{
           //1.저장 시 사용했던 것과 동일한 절대경로 생성
           String ap = new File("src/main/resources/static/images/voteUpload/").getAbsolutePath();
           //2.db 저장경로에 파일명만 추출
           String fileName = filePath.substring(filePath.lastIndexOf("/")+1);
           //3. 실제 파일 객체 생성 후 삭제
           File file  = new File(ap,fileName);

           if(file.exists()){
               if(file.delete()){
                   System.out.println("기존 파일 삭제 성공: " + fileName);
               } else{
                   System.out.println("기존 파일 삭제 실패");
               }
           }
       } catch (Exception e){
           System.err.println("파일 삭제 중 에러 발생: " + e.getMessage());
       }

    }
}
