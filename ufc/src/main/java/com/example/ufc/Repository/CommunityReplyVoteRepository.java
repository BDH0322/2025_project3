package com.example.ufc.Repository;

import com.example.ufc.Entity.CommunityReplyVoteEntity;
import com.example.ufc.Entity.CommunityReplyVoteId;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommunityReplyVoteRepository extends JpaRepository<CommunityReplyVoteEntity, CommunityReplyVoteId> {

    @Query("SELECT v FROM CommunityReplyVoteEntity v WHERE v.replyNumber = :replyNumber AND v.id = :id")
    Optional<CommunityReplyVoteEntity> VotedId(
            @Param("replyNumber") Long replyNumber,
            @Param("id") String id
    );

}
