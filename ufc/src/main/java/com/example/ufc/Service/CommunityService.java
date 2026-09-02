package com.example.ufc.Service;

import com.example.ufc.Entity.CommunityEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface CommunityService {

    void contentinsert(CommunityEntity centity);

    CommunityEntity findcommunitypost(Long communityContentNumber);

    void contentmodify(CommunityEntity centity);

    void contentdelete(Long communityContentNumber);

    Page<CommunityEntity> findcommunity(Pageable pageable);

    Page<CommunityEntity> CommunitySearch(String searchType, String keyword, Pageable pageable);

    void viewCount(Long communityContentNumber);

    int vote(Long communityContentNumber, String id);

    void contentlike(Long communityContentNumber, String id);

    void contentdislike(Long communityContentNumber, String id);

    Page<CommunityEntity> getCommunityListByCategoryAndSearch(Integer category, String searchType, String keyword, Pageable pageable);

    Page<CommunityEntity> getCommunityListByCategory(Integer category, Pageable pageable);

    void hidePost(Long communityContentNumber);

    void deletepost(Long communityContentNumber);

    void unhidePost(Long communityContentNumber);

    Page<CommunityEntity> getUserCommunity(String userId, Pageable pageable);

    void banUser(String userId, int hours);

    List<CommunityEntity> postlist();
}
