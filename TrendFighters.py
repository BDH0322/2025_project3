import sys
import json
import pandas as pd
import os
import traceback # 🌟 추가: Traceback 모듈 임포트

# --- [핵심 계산 함수] ---
# fighters_list는 이미 json.loads()를 거친 파이썬 리스트입니다.
def calculate_trend_score(fighters_list, score_type):
    # 1. 데이터프레임 변환
    try:
        df = pd.DataFrame(fighters_list) 
    except Exception as e:
        # 데이터프레임 생성 오류 시 빈 리스트 반환
        return []

    # 2. 🌟 중요: None/NaN 값 0으로 대체 (결측치 처리) 🌟
    # totalLosses와 같은 컬럼이 존재하지 않을 경우를 대비하여 컬럼 존재 여부 체크 필요
    fields_to_fill = ['slpm', 'koTko', 'tdAcc', 'subAvg', 'sapm', 'totalLosses']
    
    # DataFrame에 실제로 존재하는 컬럼만 선택하여 fillna 적용
    existing_fields = [field for field in fields_to_fill if field in df.columns]
    df[existing_fields] = df[existing_fields].fillna(0)


    # 3. 점수 계산 로직 
    # 🌟 Java TrendFighters 호출 인자에 맞춰 대문자 'F'로 수정
    if score_type == 'hotFighters':
        df['score'] = (df['slpm'] * 0.7) + (df['koTko'] * 0.3)
    elif score_type == 'starFighters':
        df['score'] = (df['tdAcc'] * 0.5) + (df['subAvg'] * 0.5)
    elif score_type == 'alertFighters':
        # sapm은 피격 수이므로 점수를 낮추는 데 사용
        df['score'] = (df['sapm'] * -1.0) + (df['totalLosses'] * 0.5)
    else:
        return []

    # 4. 정렬 및 상위 6명 선택
    df = df.sort_values(by='score', ascending=False)
    # Java에서는 3명을 요구했지만, Python에서 6명을 반환하고 Java에서 3명을 선택해도 무방
    top_fighters = df.head(6)

    # 5. DTO에 없는 'score' 필드 제거 후 JSON으로 변환
    # score 컬럼이 존재하는지 확인 후 제거 (혹시 로직이 바뀌었을 경우를 대비)
    columns_to_drop = ['score'] if 'score' in top_fighters.columns else []
    
    return top_fighters.drop(columns=columns_to_drop).to_dict('records')

# --- [메인 실행부] ---
if __name__ == '__main__':
    try:
        # 1. 입력 JSON 읽기
        input_data_json_str = sys.stdin.read()
        
        # 입력 데이터가 없으면 오류 처리
        if not input_data_json_str:
            raise ValueError("No JSON data received from Java process.")
            
        input_data = json.loads(input_data_json_str)
        
        # 2. score_type 인자 가져오기
        if len(sys.argv) > 1:
            score_type = sys.argv[1]
        else:
            raise ValueError("Missing score_type argument.")

        # 3. 핵심 계산 실행 및 표준 출력
        result_list = calculate_trend_score(input_data, score_type)
        print(json.dumps(result_list))
        
        sys.exit(0)
        
    except Exception as e:
        # 🌟 오류 발생 시 에러 스트림에 상세 Traceback을 JSON 형태로 출력 🌟
        print(json.dumps({
            "error_type": type(e).__name__,
            "error_message": str(e),
            "traceback": traceback.format_exc()
        }), file=sys.stderr)
        sys.exit(1)
