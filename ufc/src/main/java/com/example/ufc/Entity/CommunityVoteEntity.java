package com.example.ufc.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "communityVote")
@IdClass(value = CommunityVoteId.class)
public class CommunityVoteEntity {

    @Id
    @Column
    Long communityContentNumber;
    @Id
    @Column
    String id;
    @Column(nullable = true)
    Long replyNumber;
    @Column
    int voteType;

    public CommunityVoteEntity(Long communityContentNumber, String id, int voteType) {
        this.communityContentNumber = communityContentNumber;
        this.id = id;
        this.voteType = voteType;
        this.replyNumber = null;
    }
}
