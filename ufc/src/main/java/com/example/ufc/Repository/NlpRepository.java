package com.example.ufc.Repository;

import com.example.ufc.Entity.NlpEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NlpRepository extends JpaRepository<NlpEntity, Long> {

    @Query(value = "SELECT * FROM nlp WHERE source_type = :type AND source_id = :id AND fighter_name = :fighterName", nativeQuery = true)
    NlpEntity existsRowWithFighter(@Param("type") String type, @Param("id") Long id, @Param("fighterName") String fighterName);

    @Query(
            value = """
            SELECT 1
            FROM NLP
            WHERE SOURCE_TYPE = :sourceType
              AND SOURCE_ID = :sourceId
              AND ROWNUM = 1
        """,
            nativeQuery = true
    )
    Integer existsRow(
            @Param("sourceType") String sourceType,
            @Param("sourceId") Long sourceId
    );

    // 선수 이름과 총 점수(가중치 합산)를 가져오는 쿼리
//    @Query("SELECT n.fighterEntity.name, SUM(n.weight + n.likeCount) as totalScore " +
//            "FROM NlpEntity n " +
//            "GROUP BY n.fighterEntity.name " +
//            "ORDER BY totalScore DESC")
//    List<Object[]> findFighterRank();

    // transformers api까지 합산
//    @Query("SELECT n.fighterEntity.name, " +
//            "SUM((n.weight * COALESCE(n.sentimentScore, 1.0)) + (n.likeCount * 0.1)) as totalScore " +
//            "FROM NlpEntity n " +
//            "GROUP BY n.fighterEntity.name " +
//            "ORDER BY totalScore DESC")
//    List<Object[]> findFighterRank();

    // 분석이 완료된 컬럼만 가져오기
//    @Query("SELECT n.fighterEntity.name, " +
//            "SUM((n.weight * n.sentimentScore) + (n.likeCount * 0.1)) as totalScore " +
//            "FROM NlpEntity n " +
//            "WHERE n.sentimentScore IS NOT NULL " + // 분석이 완료된 데이터만 랭킹에 반영
//            "GROUP BY n.fighterEntity.name " +
//            "ORDER BY totalScore DESC")
//    List<Object[]> findFighterRank();

//    @Query("SELECT n.fighterEntity.name, " +
//            "SUM((n.weight * n.sentimentScore) + (n.likeCount * 0.1)) as totalScore " +
//            "FROM NlpEntity n " +
//            "WHERE n.sentimentScore IS NOT NULL " +
//            "GROUP BY n.fighterEntity.name " +
//            "ORDER BY totalScore DESC")
//    List<Object[]> findFighterRank();

    @Query("SELECT n.fighterEntity.name, " +
            "SUM(COALESCE(n.sentimentScore, 0.0) + (COALESCE(n.likeCount, 0) * 0.1)) as totalScore " +
            "FROM NlpEntity n " +
            "WHERE n.sentimentScore IS NOT NULL " +
            "GROUP BY n.fighterEntity.name " +
            "ORDER BY totalScore DESC")
    List<Object[]> findFighterRank();
}
