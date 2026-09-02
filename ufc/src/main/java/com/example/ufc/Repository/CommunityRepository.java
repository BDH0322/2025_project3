package com.example.ufc.Repository;

import com.example.ufc.Entity.CommunityEntity;
import jakarta.transaction.Transactional;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommunityRepository extends JpaRepository<CommunityEntity, Long> {

    @Query(value = "SELECT * FROM (" +
            "    SELECT ROWNUM rn, t.* FROM (" +
            "        SELECT * FROM COMMUNITY c ORDER BY c.community_write_time DESC" +
            "    ) t " +
            ") WHERE rn > :offset AND rn <= (:offset + :pageSize)",
            nativeQuery = true)
    List<CommunityEntity> PageAll(@Param("offset") long offset, @Param("pageSize") int pageSize);

    @Query(value = "SELECT COUNT(c.community_content_number) FROM COMMUNITY c",
            nativeQuery = true)
    long countAllPosts();

    // 1. 제목 검색
    @Query(value = "SELECT * FROM (" +
            "    SELECT ROWNUM rn, t.* FROM (" +
            "        SELECT * FROM COMMUNITY c WHERE c.community_title LIKE %:keyword% ORDER BY c.community_write_time DESC" +
            "    ) t " +
            ") WHERE rn > :offset AND rn <= (:offset + :pageSize)",
            nativeQuery = true)
    List<CommunityEntity> findByCommunityTitleContaining(@Param("keyword") String keyword, @Param("offset") long offset, @Param("pageSize") int pageSize);
    // 제목 검색 카운트
    @Query(value = "SELECT COUNT(c.community_content_number) FROM COMMUNITY c WHERE c.community_title LIKE %:keyword%",
            nativeQuery = true)
    long countByCommunityTitleContaining(@Param("keyword") String keyword);


    // 2. 내용 검색
    @Query(value = "SELECT * FROM (" +
            "    SELECT ROWNUM rn, t.* FROM (" +
            "        SELECT * FROM COMMUNITY c WHERE c.community_content LIKE %:keyword% ORDER BY c.community_write_time DESC" +
            "    ) t " +
            ") WHERE rn > :offset AND rn <= (:offset + :pageSize)",
            nativeQuery = true)
    List<CommunityEntity> findByCommunityContentContaining(@Param("keyword") String keyword, @Param("offset") long offset, @Param("pageSize") int pageSize);
    // 내용 검색 카운트
    @Query(value = "SELECT COUNT(c.community_content_number) FROM COMMUNITY c WHERE c.community_content LIKE %:keyword%",
            nativeQuery = true)
    long countByCommunityContentContaining(@Param("keyword") String keyword);


    // 3. 작성자 ID 검색
    @Query(value = "SELECT * FROM (" +
            "    SELECT ROWNUM rn, t.* FROM (" +
            "        SELECT * FROM COMMUNITY c WHERE c.id LIKE %:keyword% ORDER BY c.community_write_time DESC" +
            "    ) t " +
            ") WHERE rn > :offset AND rn <= (:offset + :pageSize)",
            nativeQuery = true)
    List<CommunityEntity> findByIdContaining(@Param("keyword") String keyword, @Param("offset") long offset, @Param("pageSize") int pageSize);
    // ID 검색 카운트
    @Query(value = "SELECT COUNT(c.community_content_number) FROM COMMUNITY c WHERE c.id LIKE %:keyword%",
            nativeQuery = true)
    long countByIdContaining(@Param("keyword") String keyword);


    // 4. 제목 또는 내용 검색
    @Query(value = "SELECT * FROM (" +
            "    SELECT ROWNUM rn, t.* FROM (" +
            "        SELECT * FROM COMMUNITY c WHERE c.community_title LIKE %:keyword% OR c.community_content LIKE %:keyword% ORDER BY c.community_write_time DESC" +
            "    ) t " +
            ") WHERE rn > :offset AND rn <= (:offset + :pageSize)",
            nativeQuery = true)
    List<CommunityEntity> findByTitleOrContentContaining(@Param("keyword") String keyword, @Param("offset") long offset, @Param("pageSize") int pageSize);
    // 제목 또는 내용 검색 카운트
    @Query(value = "SELECT COUNT(c.community_content_number) FROM COMMUNITY c WHERE c.community_title LIKE %:keyword% OR c.community_content LIKE %:keyword%",
            nativeQuery = true)
    long countByTitleOrContentContaining(@Param("keyword") String keyword);

    @Modifying
    @Query("UPDATE CommunityEntity c SET c.communityViewCount = c.communityViewCount + 1 WHERE c.communityContentNumber = :communityContentNumber")
    void viewCount(@Param("communityContentNumber") Long communityContentNumber);

    @Query(value = "SELECT * FROM (" +
            "    SELECT ROWNUM rn, t.* FROM (" +
            "        SELECT * FROM COMMUNITY c WHERE c.community_category = :category AND c.community_title LIKE %:keyword% ORDER BY c.community_write_time DESC" +
            "    ) t " +
            ") WHERE rn > :offset AND rn <= (:offset + :pageSize)",
            nativeQuery = true)
    List<CommunityEntity> findByCategoryAndCommunityTitleContaining(@Param("category") Integer category, @Param("keyword") String keyword, @Param("offset") long offset, @Param("pageSize") int pageSize);

    @Query(value = "SELECT COUNT(c.community_content_number) FROM COMMUNITY c WHERE c.community_category = :category AND c.community_title LIKE %:keyword%",
            nativeQuery = true)
    long countByCategoryAndCommunityTitleContaining(@Param("category") Integer category, @Param("keyword") String keyword);

    @Query(value = "SELECT * FROM (" +
            "    SELECT ROWNUM rn, t.* FROM (" +
            "        SELECT * FROM COMMUNITY c WHERE c.community_category = :category AND c.community_content LIKE %:keyword% ORDER BY c.community_write_time DESC" +
            "    ) t " +
            ") WHERE rn > :offset AND rn <= (:offset + :pageSize)",
            nativeQuery = true)
    List<CommunityEntity> findByCategoryAndCommunityContentContaining(@Param("category") Integer category, @Param("keyword") String keyword, @Param("offset") long offset, @Param("pageSize") int pageSize);

    @Query(value = "SELECT COUNT(c.community_content_number) FROM COMMUNITY c WHERE c.community_category = :category AND c.community_content LIKE %:keyword%",
            nativeQuery = true)
    long countByCategoryAndCommunityContentContaining(@Param("category") Integer category, @Param("keyword") String keyword);

    @Query(value = "SELECT * FROM (" +
            "    SELECT ROWNUM rn, t.* FROM (" +
            "        SELECT * FROM COMMUNITY c WHERE c.community_category = :category AND c.id LIKE %:keyword% ORDER BY c.community_write_time DESC" +
            "    ) t " +
            ") WHERE rn > :offset AND rn <= (:offset + :pageSize)",
            nativeQuery = true)
    List<CommunityEntity> findByCategoryAndIdContaining(@Param("category") Integer category, @Param("keyword") String keyword, @Param("offset") long offset, @Param("pageSize") int pageSize);

    @Query(value = "SELECT COUNT(c.community_content_number) FROM COMMUNITY c WHERE c.community_category = :category AND c.id LIKE %:keyword%",
            nativeQuery = true)
    long countByCategoryAndIdContaining(@Param("category") Integer category, @Param("keyword") String keyword);

    @Query(value = "SELECT * FROM (" +
            "    SELECT ROWNUM rn, t.* FROM (" +
            "        SELECT * FROM COMMUNITY c WHERE c.community_category = :category AND (c.community_title LIKE %:keyword% OR c.community_content LIKE %:keyword%) ORDER BY c.community_write_time DESC" +
            "    ) t " +
            ") WHERE rn > :offset AND rn <= (:offset + :pageSize)",
            nativeQuery = true)
    List<CommunityEntity> findByCategoryAndTitleOrContentContaining(@Param("category") Integer category, @Param("keyword") String keyword, @Param("offset") long offset, @Param("pageSize") int pageSize);

    @Query(value = "SELECT COUNT(c.community_content_number) FROM COMMUNITY c WHERE c.community_category = :category AND (c.community_title LIKE %:keyword% OR c.community_content LIKE %:keyword%)",
            nativeQuery = true)
    long countByCategoryAndTitleOrContentContaining(@Param("category") Integer category, @Param("keyword") String keyword);

    @Query(value = "SELECT * FROM (" +
            "    SELECT ROWNUM rn, t.* FROM (" +
            "        SELECT * FROM COMMUNITY c WHERE c.community_category = :category ORDER BY c.community_write_time DESC" +
            "    ) t " +
            ") WHERE rn > :offset AND rn <= (:offset + :pageSize)",
            nativeQuery = true)
    List<CommunityEntity> findByCommunityCategory(@Param("category") Integer category, @Param("offset") long offset, @Param("pageSize") int pageSize);

    @Query(value = "SELECT COUNT(c.community_content_number) FROM COMMUNITY c WHERE c.community_category = :category",
            nativeQuery = true)
    long countByCommunityCategory(@Param("category") Integer category);

//    @Query(
//            value =
//                    "SELECT * FROM (" +
//                            "   SELECT a.*, ROWNUM rnum FROM (" +
//                            "       SELECT * FROM community " +
//                            "       WHERE id = :userId " +
//                            "       ORDER BY community_content_number DESC" +
//                            "   ) a WHERE ROWNUM <= :end" +
//                            ") WHERE rnum > :start",
//            nativeQuery = true
//    )
//    List<CommunityEntity> findUserCommunity(
//            @Param("userId") String userId,
//            @Param("start") int start,
//            @Param("end") int end
//    );

    @Query(value = "SELECT * FROM (" +
            "    SELECT ROWNUM rn, t.* FROM (" +
            "        SELECT * FROM COMMUNITY c WHERE c.id = :userId ORDER BY c.community_write_time DESC" +
            "    ) t " +
            ") WHERE rn > :offset AND rn <= (:offset + :pageSize)",
            nativeQuery = true)
    List<CommunityEntity> findUserCommunity(
            @Param("userId") String userId,
            @Param("offset") long offset,
            @Param("pageSize") int pageSize
    );

//    @Query(
//            value = "SELECT COUNT(*) FROM community WHERE id = :userId",
//            nativeQuery = true
//    )
//    long countUserCommunity(@Param("userId") String userId);

    @Query(value = "SELECT COUNT(c.community_content_number) FROM COMMUNITY c WHERE c.id = :userId",
            nativeQuery = true)
    long countUserCommunity(@Param("userId") String userId);

    @Modifying
    @Transactional
    @Query("UPDATE MemberEntity m SET m.banEndDate = :banEndDate WHERE m.id = :userId")
    void updateBanEndDate(@Param("userId") String userId, @Param("banEndDate") LocalDateTime banEndDate);

    @Query(value = "SELECT * FROM (" +
            "  SELECT * FROM community " +
            "  WHERE community_like >= :likeCount " +
            "  ORDER BY community_like DESC" +
            ") WHERE ROWNUM <= 5",
            nativeQuery = true)
    List<CommunityEntity> findpost(@Param("likeCount") int likeCount);

    @Query(value = "SELECT * FROM ( " +
            "  SELECT a.*, ROWNUM AS rnum FROM ( " +
            "    SELECT * FROM COMMUNITY " +
            "    WHERE COMMUNITY_TITLE LIKE '%' || :keyword1 || '%' " +
            "       OR COMMUNITY_CONTENT LIKE '%' || :keyword2 || '%' " +
            "    ORDER BY COMMUNITY_WRITE_TIME DESC " +
            "  ) a WHERE ROWNUM <= :#{#pageable.offset + #pageable.pageSize} " +
            ") WHERE rnum > :#{#pageable.offset} --",
            countQuery = "SELECT count(*) FROM COMMUNITY WHERE COMMUNITY_TITLE LIKE '%' || :keyword1 || '%' OR COMMUNITY_CONTENT LIKE '%' || :keyword2 || '%'",
            nativeQuery = true)
    Page<CommunityEntity> findCommunityPost(@Param("keyword1") String keyword1, @Param("keyword2") String keyword2, Pageable pageable);

    @Query(
            value = """
        SELECT *
        FROM COMMUNITY
        WHERE COMMUNITY_WRITE_TIME > :time
    """,
            nativeQuery = true
    )
    List<CommunityEntity> CommunityWriteTimeAfter(
            @Param("time") LocalDateTime time
    );
}
