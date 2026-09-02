package com.example.ufc.Repository;

import com.example.ufc.Entity.VoteBoardEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param; // 수정: Mybatis용이 아닌 Spring용 사용
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoteBoardRepository extends JpaRepository<VoteBoardEntity,Long> {

    // boardNum으로 게시글 단건 조회
    Optional<VoteBoardEntity> findByBoardNum(Long boardNum);

    // [핵심 로직] 특정 체급 범위(예: 1000~1999) 내에서 현재 가장 큰 boardNum을 조회
    // v.baordNum -> v.boardNum 으로 수정
    // @Param("strat") -> @Param("start") 으로 수정
    @Query("SELECT MAX(v.boardNum) FROM VoteBoardEntity v WHERE v.boardNum >= :start AND v.boardNum < :end")
    Long findMaxBoardNumByRange(@Param("start") Long start, @Param("end")Long end);

    @Query(value = "SELECT * FROM vote_board ORDER BY board_num DESC", nativeQuery = true)
    List<VoteBoardEntity> findAllNative();

    @Query(value = "SELECT * FROM vote_board ORDER BY board_num DESC", nativeQuery = true)
    List<VoteBoardEntity> findAllByOrderByBoardNumDesc();

    // 1. NLP 분석용 (날짜 비교는 Native SQL에서도 > 로 처리 가능)
    @Query(value = "SELECT * FROM vote_board WHERE create_at > :time", nativeQuery = true)
    List<VoteBoardEntity> findByCreateAtAfter(@Param("time") LocalDateTime time);

    // 2. 통합 검색용 (Oracle 11g 이하용 ROWNUM 페이징 쿼리)
    @Query(value = "SELECT * FROM (" +
            "  SELECT a.*, ROWNUM rnum FROM (" +
            "    SELECT * FROM vote_board " +
            "    WHERE title LIKE %:keyword% OR content LIKE %:keyword% " +
            "    ORDER BY create_at DESC" +
            "  ) a WHERE ROWNUM <= :#{#pageable.offset + #pageable.pageSize}" +
            ") WHERE rnum > :#{#pageable.offset}",
            nativeQuery = true)
    Page<VoteBoardEntity> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

}
