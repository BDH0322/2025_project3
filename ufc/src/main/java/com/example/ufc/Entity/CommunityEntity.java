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
@Table(name = "community")
@SequenceGenerator(name = "community_seq",
sequenceName = "communityContentNumber",
allocationSize = 1,
initialValue = 1000)
public class CommunityEntity {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
    generator = "community_seq")
    private Long communityContentNumber;
    @Column
    private String id;
    @Column
    private int communityCategory;
    @Column
    private String communityTitle;
    @Column
    private String communityOriginalTitle;
    @Column
    private String communityContent;
    @Column
    private String communityImage;
    @Column
    private int communityViewCount;
    @Column
    private int communityLike;
    @Column
    private int communityDisLike;
    @Column
    private int communityCommentCount;
    @Column
    private LocalDateTime communityWriteTime;
    @Column
    private LocalDateTime communityWriteModifyTime;
    @Column
    private int admin;
    @Column
    private int communityHidden = 0;
    @Column
    private int communityPin = 0;
    @Column
    private int communityIsNotice = 0;

}
