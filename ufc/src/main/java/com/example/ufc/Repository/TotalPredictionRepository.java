package com.example.ufc.Repository;

import com.example.ufc.Entity.TotalPredictionEntity;
import com.example.ufc.Entity.VoteBoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TotalPredictionRepository extends JpaRepository<TotalPredictionEntity, Long> {
    // [추가] 게시글 엔티티를 이용해 분석 데이터를 찾는 메서드
    Optional<TotalPredictionEntity> findByVoteBoard(VoteBoardEntity voteBoard);
}
