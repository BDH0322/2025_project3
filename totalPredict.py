from fastapi import FastAPI
from pydantic import BaseModel
import torch
import torch.nn as nn

app = FastAPI()

# 1.모델 덩의
class CombinedPredictor(nn.Module):
    def __init__(self):
        super(CombinedPredictor, self).__init__()


        '''
        # TensorFlow (입력 크기를 자동으로 추론함)
            model.add(layers.Dense(16, activation='relu'))
        '''
        # [Layer 1] Dense(16 nodes) : 입력은 3개(AI, 유저비율, 투표수)
        self.dense1 = nn.Linear(3,16)

        # [Layer 2] Dense(8 nodes)
        self.dense2 = nn.Linear(16,8)

        # [Output Layer] Dense(1 node) : 0~1 사이의 확률값
        self.output = nn.Linear(8,1)

        #활성화 함수 및 출력 함수
        self.relu = nn.ReLU()
        self.sigmoid = nn.Sigmoid()


    def forward(self,x):
        #x는 [ai_score,user_rate,total_votes] 형태의 텐서
        x = self.relu(self.dense1(x))
        x = self.relu(self.dense2(x))
        x = self.sigmoid(self.output(x))
        return x
# 모델 객체 생성
model = CombinedPredictor()    

# model.load_state_dict(torch.load('model_weight.pth')) # 나중에 학습된 파일 로드 시 사용

#[중요] 아직 학습데이터가 없으므로 "임시 가중치" 설정
# ai점수에서 0.4, 유저비율에 0.6 정도의 비중을 갖도록 수학적으로 강제 설정
with torch.no_grad():
    #간단하게 입력값의 가중 평균을 내도록 초기화(예시)
    model.dense1.weight.fill_(0.1)
    model.dense1.bias.fill_(0.0)

model.eval() #추론 모드

# 입력 데이터 형식 정의
class PredictionRequest(BaseModel):
    ai_score: float
    user_rate: float
    total_votes: int
@app.post("/predict")
async def predict(req: PredictionRequest):
    #입력을 텐서로 변환[ai_score,user_rate,total_votes]
    #Spring에서 받은 데이터를 텐서로 변환
    
    input_data = [req.ai_score,req.user_rate, float(req.total_votes)]
    input_tensor = torch.tensor([input_data], dtype=torch.float32)

    with torch.no_grad():
        prediction = model(input_tensor)

    #결과값 반환
    return {"combined_score": float(prediction.item())}

#서버 실행 명령어: uvicorn main:app --reload
#서버 실행
if __name__ == "__main__":
    import uvicorn
    # 실행 버튼(▶)만 누르면 8000번 포트에서 서버가 시작됩니다.
    uvicorn.run(app, host="0.0.0.0", port = 8000)
