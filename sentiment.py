import pandas as pd
from sqlalchemy import create_engine, text
from transformers import pipeline

# db와 파이썬을 잇는 객체
engine = create_engine('oracle+cx_oracle://bdh:1234@localhost:1521/?service_name=orcl')
# from transformers import pipeline - classifier는 수만 개의 긍정/부정 문장을 미리 학습한 AI 모델
# model="beomi/kecbert-base-continuing-pretrain" 는 한국어 뉴스, 댓글, 커뮤니티 글을 엄청나게 공부한 AI 모델의 이름입니다.
# transformers api를 사용할 때 classifier = pipeline( 여기까진 고정인데
# 그 다음 가이드 라인과 인공지능은 따로 설정
classifier = pipeline("sentiment-analysis", model="jaehyeong/koelectra-base-v3-generalized-sentiment-analysis")

def update_sentiment_scores():
    query = "SELECT NLP_NUMBER, TEXT, SOURCE_TYPE, WEIGHT FROM NLP WHERE SENTIMENT_SCORE IS NULL AND TEXT IS NOT NULL"
    df = pd.read_sql(query, engine)

    if df.empty:
        print("테이블에 데이터가 없음")
        return

    print(f"{len(df)}개의 데이터 분석 시작...")

    for index, row in df.iterrows():
        nlp_num = row['nlp_number']
        nlp_text = row['text'][:512] # db의 text컬럼을 가져옴[512자 까지]
        source_type = row.get('source_type')
        weight = row.get('weight')

        if source_type in ['SEARCH_LOG', 'COMMUNITY_TITLE']:
            final_score = float(weight) # 검색(3), 제목(2), 댓글답글(1)
            sentiment_label = 'positive'

        # 4. AI 분석 수행
        # 여기서 키워드로 긍정/부정 구분
        else:
            result = classifier(nlp_text)[0] # 학습된 classifier 안에 nlp_text 구분해서 -> result
            label = result['label'] # 구분된 result -> 0 or 1
            score = result['score'] # 구분된 result 모델 결과 예시 : {'label': 'positive', 'score': 0.98}로 점수주기
            # 긍/부정의 점수는 ai가 판별해서 준다고함
            
            if label == '1':
                final_score = float(weight) # 원래 score(-1~1)인데 원하는 값 으로 고정
                sentiment_label = 'positive'
            else:
                final_score = float(-weight) # 원래 score(-1~1)인데 원하는 값 으로 고정
                sentiment_label = 'negative'

        # 5. db 업데이트
        update_query = text(f'''
                UPDATE NLP
                SET SENTIMENT = '{sentiment_label}',
                    SENTIMENT_SCORE = {final_score},
                    ANALYZED_AT = CURRENT_TIMESTAMP
                WHERE NLP_NUMBER = {nlp_num}
                ''')
        with engine.connect() as conn:
            conn.execute(update_query)
            conn.commit()

    print("분석 완료 + db 업데이트 완료")

if __name__ == "__main__":
    update_sentiment_scores()
