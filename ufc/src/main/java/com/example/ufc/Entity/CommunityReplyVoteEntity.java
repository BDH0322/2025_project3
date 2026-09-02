package com.example.ufc.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "CommunityReplyVote")
@IdClass(value = CommunityReplyVoteId.class)
public class CommunityReplyVoteEntity {

    @Id
    @Column
    private Long replyNumber;
    @Id
    @Column
    private String id;
    @Column
    private int voteStatus;
}
