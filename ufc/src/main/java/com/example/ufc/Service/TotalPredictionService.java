package com.example.ufc.Service;

import com.example.ufc.Entity.VoteBoardEntity;

public interface TotalPredictionService {

    public void saveInitialAiScore(VoteBoardEntity vote);
    public void finalizePrediction(Long boardNum);
    public void updateActualWinner(Long boardNum, Integer winner);

}
