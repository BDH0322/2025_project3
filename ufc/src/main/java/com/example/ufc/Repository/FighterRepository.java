package com.example.ufc.Repository; // 패키지명은 소문자 관례에 맞춰 수정

import com.example.ufc.Entity.FighterEntity; // 👈 Entity 이름 import
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository는 반드시 'interface'로 선언되어야 합니다.
 * JpaRepository<Entity 이름, Primary Key 타입>
 */
@Repository
public interface FighterRepository extends JpaRepository<FighterEntity, String> {

    // 선수 이름으로 찾기 위해 이 메서드를 추가합니다.
    // 엔티티의 이름 필드가 'name'이라고 가정합니다.
    // 만약 필드명이 'fighterName'이라면 findByFighterName으로 바꿔야 합니다.
    Optional<FighterEntity> findByName(String name);

    /**
     * 특정 체급(weightClass)에 해당하는 모든 FighterEntity 목록을 조회합니다.
     * Spring Data JPA가 메서드 이름(findByWeightClass)을 해석하여 쿼리를 자동 생성합니다.
     */
    List<FighterEntity> findByWeightClass(String weightClass);
    // 이 Repository는 이제 Oracle DB의 FIGHTER 테이블에 연결되어 동작합니다.

    @Query(value = "SELECT * FROM FIGHTER WHERE WEIGHT_CLASS = 'Heavyweight' AND RANK_NUM = 'C'",
            nativeQuery = true)
    FighterEntity heavyChamp();

    // 통계
    // 1. 핫 이슈 선수
    @Query(value = "SELECT * FROM (" +
            "    SELECT FIGHTER.*, (WINS * 1.0 / TOTAL) AS WIN_RATE " +
            "    FROM FIGHTER " +
            "    WHERE TOTAL > 0 " +
            "    ORDER BY WIN_RATE DESC" +
            ") WHERE ROWNUM <= 6",
            nativeQuery = true)
    List<FighterEntity> hotIssue();

    // 2. 뜨는 선수
    @Query(value = "SELECT * FROM (" +
            "    SELECT FIGHTER.*, (WINS * 1.0 / TOTAL) AS WIN_RATE " +
            "    FROM FIGHTER " +
            "    WHERE TOTAL > 0 " +
            "    ORDER BY WIN_RATE DESC, TOTAL ASC" +
            ") WHERE ROWNUM <= 6",
            nativeQuery = true)
    List<FighterEntity> risingStar();

    // 디깅
    // 1. 속전속결 (짧은 평균 경기 시간) - ASC
    @Query(value = "SELECT * FROM (" +
            "    SELECT FIGHTER.* " +
            "    FROM FIGHTER " +
            "    WHERE WEIGHT_CLASS = :weightClass AND AVG_TIME IS NOT NULL " +
            "    ORDER BY AVG_TIME ASC" +
            ") WHERE ROWNUM <= 6",
            nativeQuery = true)
    List<FighterEntity> findDiggingQuickFinish(@Param("weightClass") String weightClass);

    // 2. 예절 주입기 (높은 KO/TKO 횟수) - DESC
    @Query(value = "SELECT * FROM (" +
            "    SELECT FIGHTER.* " +
            "    FROM FIGHTER " +
            "    WHERE WEIGHT_CLASS = :weightClass AND KO_TKO > 0 " +
            "    ORDER BY KO_TKO DESC" +
            ") WHERE ROWNUM <= 6",
            nativeQuery = true)
    List<FighterEntity> findDiggingHighKnockout(@Param("weightClass") String weightClass);

    // 3. 빠른 손 (높은 SLPM) - DESC
    @Query(value = "SELECT * FROM (" +
            "    SELECT FIGHTER.* " +
            "    FROM FIGHTER " +
            "    WHERE WEIGHT_CLASS = :weightClass AND SLPM IS NOT NULL " +
            "    ORDER BY SLPM DESC" +
            ") WHERE ROWNUM <= 6",
            nativeQuery = true)
    List<FighterEntity> findDiggingHighSlpm(@Param("weightClass") String weightClass);

    // 4. 마조히스트 (높은 SAPM) - DESC
    @Query(value = "SELECT * FROM (" +
            "    SELECT FIGHTER.* " +
            "    FROM FIGHTER " +
            "    WHERE WEIGHT_CLASS = :weightClass AND SAPM IS NOT NULL " +
            "    ORDER BY SAPM DESC" +
            ") WHERE ROWNUM <= 6",
            nativeQuery = true)
    List<FighterEntity> findDiggingHighSapm(@Param("weightClass") String weightClass);

    // 5. 대걸레 (높은 TD ACC) - DESC
    @Query(value = "SELECT * FROM (" +
            "    SELECT FIGHTER.* " +
            "    FROM FIGHTER " +
            "    WHERE WEIGHT_CLASS = :weightClass AND TDACC IS NOT NULL " +
            "    ORDER BY TDACC DESC" +
            ") WHERE ROWNUM <= 6",
            nativeQuery = true)
    List<FighterEntity> findDiggingHighTdAcc(@Param("weightClass") String weightClass);

    // 6. 스턴건 (높은 KO/TKO 횟수) - DESC
    @Query(value = "SELECT * FROM (" +
            "    SELECT FIGHTER.* " +
            "    FROM FIGHTER " +
            "    WHERE WEIGHT_CLASS = :weightClass AND KO_TKO > 0 " +
            "    ORDER BY KO_TKO DESC" +
            ") WHERE ROWNUM <= 6",
            nativeQuery = true)
    List<FighterEntity> findDiggingStunGun(@Param("weightClass") String weightClass);

    // 7. 모범생 (높은 승률) - DESC
    @Query(value = "SELECT * FROM (" +
            "    SELECT FIGHTER.*, (WINS * 1.0 / TOTAL) AS WIN_RATE " +
            "    FROM FIGHTER " +
            "    WHERE WEIGHT_CLASS = :weightClass AND TOTAL > 0 " +
            "    ORDER BY WIN_RATE DESC" +
            ") WHERE ROWNUM <= 6",
            nativeQuery = true)
    List<FighterEntity> findDiggingHighWinRate(@Param("weightClass") String weightClass);

    // 8. 양아치 (낮은 승률) - ASC
    @Query(value = "SELECT * FROM (" +
            "    SELECT FIGHTER.*, (WINS * 1.0 / TOTAL) AS WIN_RATE " +
            "    FROM FIGHTER " +
            "    WHERE WEIGHT_CLASS = :weightClass AND TOTAL > 0 " +
            "    ORDER BY WIN_RATE ASC" +
            ") WHERE ROWNUM <= 6",
            nativeQuery = true)
    List<FighterEntity> findDiggingLowWinRate(@Param("weightClass") String weightClass);

    // 체급 목록을 가져오기 위한 쿼리 (디깅 랜덤 체급 선택에 사용)
    @Query(value = "SELECT DISTINCT WEIGHT_CLASS FROM FIGHTER WHERE WEIGHT_CLASS IS NOT NULL",
            nativeQuery = true)
    List<String> weightClass();

    @Query(value = """
    SELECT *
    FROM (
        SELECT f.*,
               ROW_NUMBER() OVER (
                   ORDER BY
                       CASE
                           WHEN f.RANK_NUM = 'C' THEN 0
                           ELSE 1
                       END,
                       CASE
                           WHEN f.RANK_NUM = 'C' THEN 0
                           ELSE TO_NUMBER(f.RANK_NUM)
                       END
               ) AS rn
        FROM FIGHTER f
        WHERE f.WEIGHT_CODE = :weightCode
          AND (
              f.RANK_NUM = 'C'
              OR REGEXP_LIKE(f.RANK_NUM, '^(1|2|3|4|5|6|7|8|9|10)$')
          )
    )
    ORDER BY rn
    """,
            nativeQuery = true)
    List<FighterEntity> findByWeightCode(@Param("weightCode") int weightCode);

    @Query("SELECT f FROM FighterEntity f WHERE f.rankNum = 'C'")
    List<FighterEntity> champions();

    @Query(value = "SELECT * FROM ( " +
            "  SELECT a.*, ROWNUM AS rnum FROM ( " +
            "    SELECT * FROM FIGHTER " +
            "    WHERE NAME LIKE '%' || :keyword || '%' " +
            "    ORDER BY NAME ASC " +
            "  ) a WHERE ROWNUM <= :#{#pageable.offset + #pageable.pageSize} " +
            ") WHERE rnum > :#{#pageable.offset} --",
            countQuery = "SELECT count(*) FROM FIGHTER WHERE NAME LIKE '%' || :keyword || '%'",
            nativeQuery = true)
    Page<FighterEntity> findFighter(@Param("keyword") String keyword, Pageable pageable); // -> IntegratedSearchServiceImp
}