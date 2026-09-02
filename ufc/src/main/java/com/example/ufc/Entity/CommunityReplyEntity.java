package com.example.ufc.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "community_reply")
@SequenceGenerator(name = "communityreply_seq",
sequenceName = "replyNumber",
allocationSize = 1,
initialValue = 10000)
public class CommunityReplyEntity {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
    generator = "communityreply_seq")
    private Long replyNumber; // 시퀀스 처리
    @Column
    private Long communityContentNumber; // 받아오기
    @Column
    private String id; // 받아오기
    @Column
    private String replyContent; // dto
    @Column
    private String replyOriginalContent;
    @Column
    private LocalDateTime replyTime; // sql
    @Column
    private LocalDateTime replyModifyTime; // sql
    @Column
    private int replyLike; // html+sql
    @Column
    private int replyDisLike;
    @Column
    private Long parentReplyNumber; // dto
    @Column
    private int replyHidden; // html+sql
}
