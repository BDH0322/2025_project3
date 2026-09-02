document.addEventListener('DOMContentLoaded', function(){

    const weightSelect = document.getElementById('weight-class-select');
    const fighter1Select = document.getElementById('fighter1-select');
    const fighter2Select = document.getElementById('fighter2-select');
    const analyzeButton = document.getElementById('analyze-button');
    const resultArea = document.getElementById('result-area');

    const f1Bar = document.querySelector('.blue-bar');
    const f2Bar = document.querySelector('.red-bar');
    const f1Text = document.getElementById('f1-prediction-text');
    const f2Text = document.getElementById('f2-prediction-text');
    const fighter1Info = document.getElementById('fighter1-info');
    const fighter2Info = document.getElementById('fighter2-info');


    // 사이드바 관련요소 및 데이터 변수 정의
    const f1Card = document.getElementById('f1-stat-card');
    const f2Card = document.getElementById('f2-stat-card');
    let currentFightersData =[]; //전체 선수 데이터를 저장할 배열

    weightSelect.addEventListener('change',function(){
        const selectedClass = this.value;
        if(!selectedClass) return;
        //초기화
        resetSelection();
        //java controller 의 /predict/fighters/{weightClass} 호출
        fetch(`/predict/fighters/${selectedClass}`)
            .then(response =>{
                if(!response.ok){
                    return response.json().then(errorData=>{
                        throw new Error(errorData.error || '선수 목록 로드 실패.서버 연결 확인');
                    });
                }
                return response.json();
            })
            .then(fightersData =>{
                   currentFightersData = fightersData;// ⬅️ DTO 객체 배열 저장

                   // 💡 수정: 객체 배열에서 name 속성을 추출하여 문자열 배열로 만듭니다.
                   //    fightersData의 각 요소는 객체(FighterDTO)이므로 f.name을 호출해야 합니다.
                   const fightersNames = fightersData.map(f=>f.name);

                   populateFighterSelect(fighter1Select, fightersNames); //이름 배열 전달
                   populateFighterSelect(fighter2Select, fightersNames); //이름 배열 전달

                   fighter1Select.disabled = false;
                   fighter2Select.disabled = false;
                   //analyzeButton.disabled = true;
                   //fighter1Info.textContent = "선수를 선택하세요";
                   //fighter2Info.textContent = "선수를 선택하세요";
                   //resultArea.classList.add('hidden');

                   // checkSelection이 위에서의 대부분의 UI 상태를 관리하도록 위임
                   checkSelection();
            })
            .catch(error =>{
                console.error('Error fetching fighters:',error);
                alert(`선수 목록을 불러오는데 실패함: ${error.message}`);
            });
    });

    //드롭다운 옵션 채우기 함수
    function populateFighterSelect(selectElement, names){
        selectElement.innerHTML='<option value="" disabled selected>선수 선택</option>';
        names.forEach(name =>{
            const option = document.createElement('option');
            option.value = name;
            option.textContent = name;
            selectElement.appendChild(option);
        });
    }
    // 사이드바 업데이트 함수
    function updateSidebar(side,fighterName){
        const card = side === 'blue' ? f1Card : f2Card;
        const prefix = side ==='blue' ? 'f1' : 'f2';

        if(!fighterName){
            card.classList.add('hidden'); //선택 취소 시 카드 숨김
            return;
        }
        //저장된 전체 데이터에서 해당 선수 객체를 찾음
        const fighterData = currentFightersData.find(f=>f.name === fighterName);

        if(fighterData){
            //DOM요소 업데이트
            document.getElementById(`${prefix}-image`).src = fighterData.imageUrl || '/images/default_fighter.png';
            document.getElementById(`${prefix}-name-display`).textContent = fighterData.name;
            document.getElementById(`${prefix}-rank-display`).textContent = fighterData.rankNum || 'Unranked';


            // 4. 전적: '승:XX - 패:YY' 포맷으로 조합 (요청하신 형태로 수정)
            const wins = fighterData.totalWins != null? fighterData.totalWins : 0;
            const losses = fighterData.totalLosses != null? fighterData.totalLosses : 0;
            const recordText = `승:${wins} - 패:${losses}`
            // 🥊 전적 표시 위치: record-display는 🥊 아이콘 옆에 표시되어야 합니다.
            document.getElementById(`${prefix}-record-display`).textContent = recordText;
            card.classList.remove('hidden'); // 카드 보여주기
        }

    }

    //2.선수 선택 시 분석 버튼 및 정보 업데이트
    function checkSelection(){


        // 선택된 옵션의 텍스트를 가져오고, '선수선택' 옵션이면 null처리
        const f1Option = fighter1Select.options[fighter1Select.selectedIndex];
        const f2Option = fighter2Select.options[fighter2Select.selectedIndex];

        const f1SelectedName = f1Option.value ? f1Option.textContent : null;
        const f2SelectedName = f2Option.value ? f2Option.textContent : null;

        //버튼 활성화를 위해 value 사용(선수이름)
        const f1SelectedValue = fighter1Select.value;
        const f2SelectedValue = fighter2Select.value;

        fighter1Info.textContent = f1SelectedName ? f1SelectedName : "선택 대기 중";
        fighter2Info.textContent = f2SelectedName ? f2SelectedName : "선택 대기 중";
        updateSidebar('blue',f1SelectedName);
        updateSidebar('red', f2SelectedName);
       // 두 선수가 모두 선택되었고, 서로 다른 선수일 때만 활성화
               if (f1SelectedName && f2SelectedName && f1SelectedName !== f2SelectedName) {
                   analyzeButton.disabled = false;
               } else {
                   analyzeButton.disabled = true;
               }
    }
    fighter1Select.addEventListener('change',checkSelection);
    fighter2Select.addEventListener('change',checkSelection);

    //3.분석하기 버튼 클릭 시 예측 요청(java Controller 호출)
    analyzeButton.addEventListener('click',function(){
        const fighter1 = fighter1Select.value;
        const fighter2 = fighter2Select.value;

        analyzeButton.textContent = "⚔️ 분석 중...";
        analyzeButton.disabled = true;

        //java Controller의 /predict/analyze 호출
        fetch('/predict/analyze',{
            method:'POST',
            headers:{
                'Content-Type':'application/json',
            },
            body:JSON.stringify({fighter1:fighter1,fighter2:fighter2}),
        })
        .then(response =>{
            if(!response.ok){
              return response.json().then(errorData => { throw new Error(errorData.error || '서버 측 오류가 발생했습니다.'); });
            }
            return response.json();
        })
        .then(data =>{
            //4.예측 결과 업데이트 및 표시
            const probF1 = data.prob_f1*100;
            const probF2 = data.prob_f2*100;

            // 🛑 [4] f1Bar: 닫는 백틱 위치 오류 수정 (치명적)
                        f1Bar.style.width = `${probF1.toFixed(2)}%`;
                        // 🛑 [5] f2Bar: f1Bar에 probF2를 할당하던 논리 오류 수정
                        f2Bar.style.width = `${probF2.toFixed(2)}%`;

                        // 텍스트 업데이트 (이 부분은 올바릅니다)
                        f1Text.innerHTML = `<strong>${data.fighter1}</strong> 승리 확률: ${probF1.toFixed(2)}%`;
                        // 🛑 [6] f2Text: probF1이 아닌 probF2를 사용하도록 수정 (논리 오류)
                        f2Text.innerHTML = `<strong>${data.fighter2}</strong> 승리 확률: ${probF2.toFixed(2)}%`;
            resultArea.classList.remove('hidden');
            resultArea.scrollIntoView({ behavior: 'smooth' });
            analyzeButton.textContent ="⚔️ 분석 완료";
        })
        .catch(error =>{
            console.error('Prediction Error',error);
            alert(`예측 요청 실패:${error.message}`);
            analyzeButton.textContent = "⚔️ 분석하기";
        })
        .finally(() => {

            checkSelection();
        });

    });

    // 초기화 함수(resetSelection) 정의
    function resetSelection(){
        fighter1Select.value = "";
        fighter2Select.value = "";
        currentFightersData = []; //데이터 초기화

         //UI 초기화
         f1Card.classList.add('hidden'); //사이드바 카드 숨기기
         f2Card.classList.add('hidden');
         resultArea.classList.add('hidden');
         analyzeButton.disabled = true;

         fighter1Info.textContent = "체급 선택 후 활성화";
         fighter2Info.textContent = "체급 선택 후 활성화";

         //드롭다운 내용 초기화 (옵션리셋)
         fighter1Select.innerHTML='<option value="" disabled selected>선수선택</option>';
         fighter2Select.innerHTML='<option value="" disabled selected>선수 선택</option>';
         fighter1Select.disabled = true;
         fighter2Select.disabled = true;
    }
});