import multiprocessing
import uvicorn
import time

# 1. Flask 서버 실행 함수 (ufc_data_springboot.py의 내용을 가져옴)
def run_flask():
    from ufc_data_springboot import app as flask_app
    print("--- Flask 서버(5000포트) 시작 중 ---")
    flask_app.run(host='0.0.0.0', port=5000, debug=False, use_reloader=False)

# 2. FastAPI 서버 실행 함수 (totalPredict.py의 내용을 가져옴)
def run_fastapi():
    from totalPredict import app as fastapi_app
    print("--- FastAPI 서버(8000포트) 시작 중 ---")
    uvicorn.run(fastapi_app, host="0.0.0.0", port=8000)

if __name__ == "__main__":
    # 프로세스 생성
    flask_process = multiprocessing.Process(target=run_flask)
    fastapi_process = multiprocessing.Process(target=run_fastapi)

    # 서버들 시작
    flask_process.start()
    time.sleep(2)  # Flask가 모델 로드할 시간을 잠시 줌
    fastapi_process.start()

    print("\n✅ 두 개의 서버가 모두 실행되었습니다.")
    print("- Flask: http://localhost:5000")
    print("- FastAPI: http://localhost:8000\n")

    try:
        flask_process.join()
        fastapi_process.join()
    except KeyboardInterrupt:
        print("\n--- 서버를 종료합니다 ---")
        flask_process.terminate()
        fastapi_process.terminate()
