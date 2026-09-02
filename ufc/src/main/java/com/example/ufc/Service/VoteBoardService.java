package com.example.ufc.Service;

import com.example.ufc.DTO.VoteBoardDTO;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VoteBoardService {
    void createVote(VoteBoardDTO dto, MultipartFile voteImage);
    List<VoteBoardDTO> getAllVotes();
    VoteBoardDTO getVoteByBoardNum(Long boardNum);
    public void updateVote(Long boardNum, VoteBoardDTO dto, MultipartFile voteImage);
    public void deleteVote(Long boardNum);
    void closeVote(Long boardNum);
    Page<VoteBoardDTO> getVoteList(int page,String keyword);
    void addVoteWithCheck(Long boardNum, int fighterNum,String userId);
    boolean checkIfVoted(Long boardNum, String name);
}
