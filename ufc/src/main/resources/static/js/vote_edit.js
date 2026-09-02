
window.addEventListener('DOMContentLoaded',async function(){
    const weightSelect = document.getElementById('weightClass');
    //HTML th:value로 박혀있는 원래 선수 이름 가져오기
    const originalF1 = document.getElementById('originalFighter1')?.value;
    const originalF2 = document.getElementById('originalFighter2')?.value;

    //1.페이지 로드 시 기존 체급이 있다면 선수 목록 즉시 로드
    if(weightSelect && weightSelect.value){
        await loadFighters(weightSelect.value, originalF1, originalF2);
    }
    //2. 체급 변경 시 선수 목록 갱신 이벤트
    weightSelect.addEventListener('change', async function(){
        await loadFighters(this.value);

    });
});
//선수 목록 로드 공통 함수

async function loadFighters(weightClass, sel1=null,sel2=null){
    const f1Select = document.getElementById('fighter1');
    const f2Select = document.getElementById('fighter2');

    try{

        const response = await fetch(`/predict/fighters/${weightClass}`);
        const fighters = await response.json();

        const defaultOption = '<option value="" disabled>선수선택</option>';
        const option = fighters.map(f => `<option value="${f.name}">${f.name}</option>`).join('');

        f1Select.innerHTML = defaultOption + option;
        f2Select.innerHTML = defaultOption + option;

        f1Select.disabled = false;
        f2Select.disabled = false;

       if(sel1) {
                   f1Select.value = sel1;
                   console.log("F1 선택 시도:", sel1, "결과:", f1Select.value);
               }
               if(sel2) {
                   f2Select.value = sel2;
                   console.log("F2 선택 시도:", sel2, "결과:", f2Select.value);
               }
    } catch(e){
        console.error("선수 목록 로드 실패")
    }
}


/**
 * [이미지 미리보기 함수]
 * 사용자가 파일을 선택했을 때(onchange) 실행됩니다.
 * @param {HTMLInputElement} input - 파일 선택 input 엘리먼트
 */
function previewImage(input){

    const container = document.getElementById('imagePreviewContainer');
    const preview = document.getElementById('imagePreview');

    if(input.files && input.files[0]){ // 실제로 파일이 존재하는지 확인

        //파일을 읽기 위한 FileReader 객체를 생성
        const reader = new FileReader();
        //파일 읽기가 완료되었을 떄 실행할 롤백 함수를 정의
        reader.onload = function(e){
            //읽어들인 파일의 (base 64경로)를 img 태그의 src에 넣는다.
            preview.src = e.target.result;
            // 숨겨져 있던 미리보기의 컨테이너를 화면에 표시
            container.style.display = 'block';
        }

        //4.파일을 Data URL 형식으로 읽어들입니다.(이 작업이 끝나면 onload가 실행된다)
        reader.readAsDataURL(input.files[0]);
    }
}

function removeImage(){
    //파일을 선택장(input)과 미리보기 컨테이너를 가져온다.
    const input = document.getElementById('voteImage');
    const container = document.getElementById('imagePreviewContainer');

    //1.input에 담긴 파일 정보를 초기화(빈문자열 설정)
    if(input){
        input.value="";
    }

    //2.화면에서 미리보기 영역을 다시 숨김
    if(container){
        container.style.display = 'none';
    }
}
