import pandas as pd
from sqlalchemy import create_engine, text
from sqlalchemy.types import Integer, String, Numeric, Date, Float # Float 임포트 유지
import os
import oracledb
import re

# ----------------------------------------------------
# 📌 A. 설정 정보 (유지)
# ----------------------------------------------------
BASE_DIR = r"C:\project"
FINAL_CSV_NAME = "UFC_ALL_FIGHTERS_FOR_DB.csv"
FINAL_FILE_PATH = os.path.join(BASE_DIR, FINAL_CSV_NAME)
DB_USER = "bdh"
DB_PASSWORD = "1234"
DB_HOST = "localhost"
DB_PORT = "1521"
DB_SERVICE = "orcl"
DB_TABLE = "FIGHTER"

# ----------------------------------------------------
# 💡 B. DTYPE 매핑 정의 (Java Entity의 Double에 맞춰 FLOAT으로 통일)
# ----------------------------------------------------
DTYPE_MAPPING = {
    'NAME': String(100),
    'HEIGHT': Float,       # FLOAT으로 유지
    'WEIGHT': Float,       # FLOAT으로 유지
    'REACH': Float,        # FLOAT으로 유지
    'STANCE': String(50),
    'DOB': Date,
    'SLPM': Float,         # FLOAT으로 유지
    'STRACC': Numeric(10, 2), # Numeric 유지
    'SAPM': Float,         # FLOAT으로 유지
    'STRDEF': Numeric(10, 2), # Numeric 유지
    'TDAVG': Float,        # FLOAT으로 유지
    'TDACC': Numeric(10, 2), # Numeric 유지
    'TDDEF': Numeric(10, 2), # Numeric 유지
    'SUBAVG': Float,       # FLOAT으로 유지
    'WEIGHT_CLASS': String(50),
    'RANK_NUM': String(10),
    'WEIGHT_CODE': Integer,
    'TOTAL': Integer,
    'WINS': Integer,
    'KO_TKO': Integer,
    'SUB_WINS': Integer,
    'DEC_WINS': Integer,
    'LOSSES': Integer,
    'DRAWS': Integer,
    'AVG_TIME': Float,     # FLOAT으로 유지
    'FIGHTER_CODE': Integer,
    'IMAGE_URL': String(500)
}

def clean_fighter_data(df):
    
    if 'HEIGHT' in df.columns:
        def convert_to_cm(height_str):
            if pd.isna(height_str) or str(height_str).strip() == '': return None
            match = re.search(r'(\d+)\'\s*(\d+)', str(height_str))
            if match:
                feet = int(match.group(1))
                inches = int(match.group(2))
                total_inches = (feet * 12) + inches
                return total_inches * 2.54
            return None
            
        df['HEIGHT'] = df['HEIGHT'].apply(convert_to_cm).round(2)
        
    if 'WEIGHT' in df.columns:
        df['WEIGHT'] = pd.to_numeric(df['WEIGHT'].astype(str).str.replace(' lbs.', '', regex=False), errors='coerce')
        df['WEIGHT'] = (df['WEIGHT'] * 0.453592).round(2)
        
    if 'REACH' in df.columns:
        df['REACH'] = pd.to_numeric(df['REACH'].astype(str).str.replace(r'[^\d.]', '', regex=True), errors='coerce')
        df['REACH'] = (df['REACH'] * 2.54).round(2)
    
    percentage_cols = ['STRACC', 'STRDEF', 'TDACC', 'TDDEF'] 
    for col in percentage_cols:
        if col in df.columns:
            df[col] = pd.to_numeric(df[col].astype(str).str.replace('%', '', regex=False), errors='coerce')

    numeric_cols = ['SLPM', 'SAPM', 'TDAVG', 'SUBAVG', 'AVG_TIME', 'TOTAL', 'WINS', 'KO_TKO', 'SUB_WINS', 'DEC_WINS', 'LOSSES', 'DRAWS', 'WEIGHT_CODE'] 
    for col in numeric_cols:
        if col in df.columns:
            df[col] = pd.to_numeric(df[col], errors='coerce')
    
    return df

def generate_fighter_code(df):
    if 'FIGHTER_CODE' in df.columns:
        df = df.drop(columns=['FIGHTER_CODE'])
    
    new_df = []
    for weight_code, group in df.groupby('WEIGHT_CODE'):
        group_sorted = group.sort_values(by=['WEIGHT_CLASS', 'RANK_NUM'], ascending=[True, True]) 
        start_num = int(weight_code) * 100
        sequence = range(1, len(group_sorted) + 1)
        codes = [start_num + seq for seq in sequence]
        group_sorted['FIGHTER_CODE'] = codes
        new_df.append(group_sorted)
        
    df_result = pd.concat(new_df).sort_index()
    df_result['FIGHTER_CODE'] = df_result['FIGHTER_CODE'].astype(int)
    return df_result


def load_csv_to_oracle():
    try:
        oracledb.init_oracle_client()
        print("✅ python-oracledb Thick Mode 활성화 완료.")
    except Exception as e:
        print(f"⚠️ Oracle Client 초기화 오류. Instant Client 경로를 확인하세요: {e}")

    try:
        df = pd.read_csv(FINAL_FILE_PATH, encoding='utf-8-sig')
        df.columns = [col.upper() for col in df.columns]

        original_len = len(df)
        print(f"✅ CSV 파일 로드 성공. 총 {original_len}개 데이터.")
        
        df = clean_fighter_data(df) 
        print("✅ 데이터 클리닝 완료 (HEIGHT, WEIGHT, REACH CM/KG 변환 완료).")

        df.dropna(subset=['NAME', 'WEIGHT_CODE'], inplace=True) 
        df = df[df['NAME'].astype(str).str.strip() != '']
        new_len = len(df)
        print(f"✅ 필수 컬럼 누락/빈 값 행 {original_len - new_len}개 제거 완료. 총 {new_len}개 데이터.")

        if 'DOB' in df.columns:
            df['DOB'] = pd.to_datetime(df['DOB'], format='%d-%b-%y', errors='coerce')
        
        integer_stat_cols = ['TOTAL', 'WINS', 'KO_TKO', 'SUB_WINS', 'DEC_WINS', 'LOSSES', 'DRAWS', 'WEIGHT_CODE']
        for col in integer_stat_cols:
             if col in df.columns:
                 df[col] = df[col].fillna(0).astype(int)

        df['IMAGE_URL'] = None
        df = generate_fighter_code(df)
        
        connection_string = (
            f"oracle+oracledb://{DB_USER}:{DB_PASSWORD}@{DB_HOST}:{DB_PORT}/?service_name={DB_SERVICE}"
        )
        engine = create_engine(connection_string)
        
        df.columns = [col.upper() for col in df.columns] 
        
        required_cols = list(DTYPE_MAPPING.keys())
        
        print("\n========================================================")
        print(f"🚨🚨🚨 최종 DataFrame 컬럼 목록 ({len(df.columns)}개):")
        print(df.columns.tolist())
        print("========================================================\n")
        
        if not all(col in df.columns for col in required_cols):
            missing_cols = [col for col in required_cols if col not in df.columns]
            print(f"\n🚨🚨🚨 최종 오류 확인: DataFrame에 없는 필수 컬럼: {missing_cols} 🚨🚨🚨")
            raise ValueError(f"삽입에 필수적인 컬럼이 누락되었습니다: {missing_cols}")

        df = df[required_cols] 
        
        final_dtype_mapping = {col: DTYPE_MAPPING[col] for col in df.columns}
        
        with engine.begin() as conn:
            
            # 🌟🌟🌟 수동으로 테이블을 생성했으므로, 기존 데이터를 삭제하는 쿼리를 다시 사용합니다. 🌟🌟🌟
            print(f"🔄 '{DB_TABLE}' 테이블의 기존 데이터를 삭제 중...")
            try:
                delete_query = text(f"DELETE FROM {DB_TABLE}")
                result = conn.execute(delete_query)
                print(f"✅ 기존 데이터 {result.rowcount}건 삭제 완료.")
            except Exception as e:
                # DELETE 오류가 발생해도 (잠금 등) 계속 진행
                print(f"⚠️ 기존 데이터 삭제 중 오류 발생: {e}. 데이터 삽입을 시도합니다.")
            
            # 테이블 구조는 건드리지 않고, 데이터만 추가합니다.
            df.to_sql(
                DB_TABLE,
                con=conn,
                if_exists='append', # 🚨 수동 생성한 테이블에 데이터만 추가 🚨
                index=False,
                chunksize=1000,
                dtype=final_dtype_mapping,
                schema=DB_USER
            )
        
        print(f"\n🎉 성공: 모든 데이터({len(df)}건)가 '{DB_TABLE}' 테이블에 저장되었습니다.")

    except ValueError as ve:
        print(f"❌ 최종 오류: 컬럼 일치 실패: {ve}")
        print("---")
        print("💡 팁: 'DTYPE_MAPPING' 변수와 DB DDL의 컬럼 이름(대문자)이 단 하나의 오타 없이 일치하는지 다시 확인해야 합니다.")
    except Exception as e:
        print(f"❌ 최종 오류: DB 연결/삽입 실패: {e}")
        print("---")
        print("💡 팁: DB 연결 정보(HOST, PORT, SERVICE, USER, PASSWORD)를 다시 확인하거나, 오라클 Instant Client가 제대로 설치되었는지 확인해 보세요.")

if __name__ == "__main__":
    load_csv_to_oracle()
