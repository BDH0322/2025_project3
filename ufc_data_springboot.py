# app.py (파이썬 Flask 서버 - 포트 5000)
from flask_cors import CORS
from flask import Flask, jsonify, request
import pandas as pd
import numpy as np
# 💡 load_model 함수를 사용하기 위해 import를 수정
from tensorflow.keras.models import load_model 
import joblib
import os 

# ----------------- 딥러닝 예측 함수 -----------------
# 💡 features 변수명 통일 및 내부 코드 수정
def predict_winner(fighter1_name, fighter2_name, model, df, scaler, features):
    """실제 예측 로직을 수행하는 함수"""
    if fighter1_name not in df['Name'].values or fighter2_name not in df['Name'].values:
        return {"error": f"데이터셋에 {fighter1_name} 또는 {fighter2_name} 선수가 없습니다."}

    f1 = df[df['Name'] == fighter1_name].iloc[0]
    f2 = df[df['Name'] == fighter2_name].iloc[0]

    # 💡 feature -> features로 통일
    X_new_diff = f1[features].values - f2[features].values
    X_new_scaled = scaler.transform(X_new_diff.reshape(1,-1))
    
    # 💡 괄호 및 문법 오류 수정
    prob_f1_win = model.predict(X_new_scaled, verbose=0)[0][0] 
    
    return {
        # 💡 "figter1" -> "fighter1" 오타 수정
        "fighter1": fighter1_name,
        "fighter2": fighter2_name,
        "prob_f1": float(prob_f1_win),
        "prob_f2": float(1 - prob_f1_win)
    }

#------모델 및 데이터 로드(서버 시작 시 1회)------------

DATA_PATH = r'C:\project\UFC_ALL_FIGHTERS_FOR_DB.csv'
MODEL_PATH = r'C:\project\ufc_predictor_model.h5'
# 💡 쉼표(,) -> 마침표(.)로 수정
SCALER_PATH = r'C:\project\ufc_scaler.pkl'

try:
    print("AI 모델 및 Scaler 로드 중...")
    # 💡 load.model -> load_model로 수정
    MODEL = load_model(MODEL_PATH)
    SCALER = joblib.load(SCALER_PATH)

    #데이터 로드 및 전처리
    DF = pd.read_csv(DATA_PATH)
    # 💡 N\A -> N/A 로 수정
    DF = DF.replace(['--','N/A'], np.nan) 
    required_cols = ['SLpM', 'StrAcc', 'SApM', 'StrDef', 'TDAvg', 'TDAcc', 'TDDef', 'SubAvg', 'TOTAL', 'WINS', 'WEIGHT_CLASS']
    DF = DF.dropna(subset=required_cols)
    # 💡 percentage_clos -> percentage_cols로 수정
    percentage_cols = ['StrAcc', 'StrDef', 'TDAcc', 'TDDef'] 
    for col in percentage_cols:
        DF[col] = DF[col].astype(str).str.replace('%', '').astype(float) / 100
    # 💡 중괄호(}) -> 대괄호(])로 수정
    DF['WinRate'] = DF['WINS']/DF['TOTAL'] 

    FEATURES = ['SLpM','StrAcc','SApM','StrDef','TDAvg','TDAcc','TDDef','SubAvg']

    print("✅ AI 모델 및 데이터 로드 완료. Flask 서버 준비됨.")

except Exception as e:
    print(f"❌ AI 모델 로드 실패: {e}")
    # 모델 로드 실패 시 서버를 종료하여 오작동을 방지
    exit()

#---flask 애플리케이션 정의
app = Flask(__name__)
CORS(app) # 모든 경로와 모든 오리진(origin)에 대해 CORS 허용

#[1] 체급별 선수 목록 제공 API
# 💡 methods['GET'] -> methods=['GET']으로 수정
@app.route('/api/fighters/<weight_class>', methods=['GET']) 
def get_fighters_by_weight(weight_class):
    filtered_df = DF[DF['WEIGHT_CLASS'].astype(str).str.upper() == weight_class.upper()]
    # 💡 .toList() -> .tolist()로 수정
    fighters = filtered_df['Name'].unique().tolist() 
    return jsonify(fighters)

    
#[2]예측 api 엔드포인트
@app.route('/api/predict', methods=['POST'])
def api_predict():
    data = request.json
    f1_name = data.get('fighter1')
    f2_name = data.get('fighter2')

    result = predict_winner(f1_name, f2_name, MODEL, DF, SCALER, FEATURES)

    if "error" in result:
        return jsonify(result),400
    return jsonify(result)

if __name__ == '__main__':
    #Flask 서버는 5000번 포트에서 구동
    # 💡 app.run() 구문 완성
    app.run(host='0.0.0.0', port=5000, debug=False)
