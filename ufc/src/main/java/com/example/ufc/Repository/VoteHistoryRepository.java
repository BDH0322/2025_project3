package com.example.ufc.Repository;

import com.example.ufc.Entity.VoteHistoryEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface VoteHistoryRepository extends JpaRepository<VoteHistoryEntity, Long> {
    // Oracle 11g 호환을 위해 Native Query로 작성합니다.
    @Query(value = "SELECT COUNT(*) FROM VOTE_HISTORY WHERE BOARD_NUM = :boardNum AND USER_ID = :userId",
            nativeQuery = true)
    int countByBoardNumAndUserIdNative(@Param("boardNum") Long boardNum, @Param("userId") String userId);

    // 서비스 레이어에서 기존처럼 boolean으로 결과를 받고 싶다면 아래 default 메서드를 추가하세요.
    default boolean existsByBoardNumAndUserId(Long boardNum, String userId) {
        return countByBoardNumAndUserIdNative(boardNum, userId) > 0;
    }
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM VOTE_HISTORY WHERE BOARD_NUM = :boardNum", nativeQuery = true)
    void deleteByBoardNumNative(@Param("boardNum") Long boardNum);

}
