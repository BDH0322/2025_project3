package com.example.ufc.Repository;

import com.example.ufc.Entity.CommunityReplyEntity;
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
public interface CommunityReplyRepository extends JpaRepository<CommunityReplyEntity, Long> {

    @Modifying
    @Transactional
    @Query("Update CommunityReplyEntity r SET r.replyContent = :replyContent, r.replyModifyTime = :replyModifyTime WHERE r.replyNumber = :replyNumber")
    void replyModify(@Param("replyNumber") Long replyNumber,
                     @Param("replyContent") String replyContent,
                     @Param("replyModifyTime") LocalDateTime replyModifyTime);

    @Query("SELECT r FROM CommunityReplyEntity r WHERE r.communityContentNumber = :communityContentNumber ORDER BY r.replyTime ASC")
    List<CommunityReplyEntity> findByCommunityContentNumber(
            @Param("communityContentNumber") Long communityContentNumber
    );

    // 관리자 댓글 숨김 쿼리 (replyHidden 0 -> 1)
    @Modifying
    @Transactional
    @Query("UPDATE CommunityReplyEntity r SET r.replyHidden = 1, r.replyOriginalContent = r.replyContent, r.replyContent = :hiddenMessage WHERE r.replyNumber = :replyNumber AND r.replyHidden = 0")
    void hideReplyAdmin(
            @Param("replyNumber") Long replyNumber,
            @Param("hiddenMessage") String hiddenMessage
    );

    // 관리자 댓글 숨김 해제 쿼리 (replyHidden 1 -> 0)
    @Modifying
    @Transactional
    @Query("UPDATE CommunityReplyEntity r SET r.replyHidden = 0, r.replyContent = r.replyOriginalContent, r.replyOriginalContent = NULL WHERE r.replyNumber = :replyNumber AND r.replyHidden = 1")
    void unhideReplyAdmin(
            @Param("replyNumber") Long replyNumber
    );

    @Query(value = "SELECT * FROM ( " +
            "  SELECT a.*, ROWNUM AS rnum FROM ( " +
            "    SELECT * FROM COMMUNITY_REPLY " +
            "    WHERE REPLY_CONTENT LIKE '%' || :keyword || '%' " +
            "    ORDER BY REPLY_TIME DESC " +
            "  ) a WHERE ROWNUM <= :#{#pageable.offset + #pageable.pageSize} " +
            ") WHERE rnum > :#{#pageable.offset} --",
            countQuery = "SELECT count(*) FROM COMMUNITY_REPLY WHERE REPLY_CONTENT LIKE '%' || :keyword || '%'",
            nativeQuery = true)
    Page<CommunityReplyEntity> findCommunityReply(@Param("keyword") String keyword, Pageable pageable);

    @Query(
            value = """
        SELECT *
        FROM COMMUNITY_REPLY
        WHERE REPLY_TIME > :time
    """,
            nativeQuery = true
    )
    List<CommunityReplyEntity> CommunityReplyWriteTimeAfter(
            @Param("time") LocalDateTime time
    );
}
