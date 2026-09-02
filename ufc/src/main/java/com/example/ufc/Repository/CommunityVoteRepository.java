package com.example.ufc.Repository;

import com.example.ufc.Entity.CommunityVoteEntity;
import com.example.ufc.Entity.CommunityVoteId;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CommunityVoteRepository extends JpaRepository<CommunityVoteEntity, CommunityVoteId> {

    @Query("SELECT v FROM CommunityVoteEntity v " +
            "WHERE v.communityContentNumber = :communityContentNumber " +
            "AND v.id = :id " +
            "AND v.replyNumber IS NULL")
    Optional<CommunityVoteEntity> VotedId(
            @Param("communityContentNumber") Long communityContentNumber,
            @Param("id") String id
    );
}
