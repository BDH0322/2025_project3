package com.example.ufc.Service;

import com.example.ufc.DTO.IntegratedSearchDTO;

public interface IntegratedSearchService {
    IntegratedSearchDTO IntegratedSearch(String keyword, int page, int size);
}
