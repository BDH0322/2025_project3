package com.example.ufc.Service;

import com.example.ufc.Repository.CommunityVoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommunityVoteServiceImp implements CommunityVoteService{

    @Autowired
    CommunityVoteRepository communityVoteRepository;
}
