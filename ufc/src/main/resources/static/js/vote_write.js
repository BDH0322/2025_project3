//1.체급 선택 시 선수 목록 가져오기 (AI 분석 페이지 로직과 동일)

document.getElementById('weightClass').addEventListener('change',async function(){
    const weightClass = this.value;
    const f1Select = document.getElementById('fighter1');
    const f2Select = document.getElementById('fighter2');

    try{
        const response = await fetch(`/predict/fighters/${weightClass}`);
        const fighters = await response.json();

        const options = fighters.map(f => `<option value="${f.name}">${f.name}</option>`).join('');
        const defaultOption = '<option value="" disabled selected>선수선택</option>';

        f1Select.innerHTML = defaultOption + options;
        f2Select.innerHTML = defaultOption + options;

        f1Select.disabled = false;
        f2Select.disabled = false;
    } catch(e){
        console.error("선수 로드 실패:",e);
    }
});



//2.폼 제출 로직
async function submitVoteForm(){
    const fightNum = document.getElementById('fightNum').value;
    const rawTitle = document.getElementById('rawTitle').value;
    const weightClass = document.getElementById('weightClass').value;
    const fighter1Name = document.getElementById('fighter1').value;
    const fighter2Name = document.getElementById('fighter2').value;

    //유효성 검사
    if(!fightNum || !rawTitle || !weightClass || !fighter1Name || !fighter2Name){
        alert("모든 필드를 입력하고 선수를 선택해주세요.");
        return;
    }
    const combinedTitle = `[UFC ${fightNum}] ${rawTitle}`;

   //FormData 생성 (html form 객체 기반으로 이미지 등 모든 필드 자동 수집)
   const formElement = document.getElementById('voteWriteForm');
   const formData = new FormData(formElement);
   //가공된 제목으로 title 필드 덮어쓰기
   formData.set('title',combinedTitle);
   try{
          const response = await fetch('/vote/vsave',{
              method: 'POST',
              //Multipart 전송 시 headers에 Content-Type을 수동으로 설정
              //브라우저가 자동으로 boundary를 포함해 설정
              body: formData // content-type은 설정 x
          });

          if(response.ok){
              alert("투표 게시글이 생성되었습니다.");
              location.href = "/vote/vlist"; // 리스트 페이지로 이동
          } else{
              const errorText = await response.text();
              alert("등록 실패: " + errorText);
          }
   } catch(error){
          console.error('전송 중 오류 발생:',error);
          alert('서버 연결 오류가 발생했습니다.')

   }
}