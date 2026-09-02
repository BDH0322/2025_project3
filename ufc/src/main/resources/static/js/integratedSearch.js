function filterSection(type, event) {
    // 0. a 태그의 기본 동작(페이지 상단 이동) 방지
    if (event) event.preventDefault();

    // 1. 모든 섹션 요소를 찾습니다.
    const sections = {
        fighters: document.querySelector('.fighters-section'),
        user: document.querySelector('.user-section'),
        community: document.querySelector('.community-section'),
        reply: document.querySelector('.reply-section'),
        vote: document.querySelector('.vote-section')
    };

    // 2. 모든 탭에서 active 클래스 제거
    document.querySelectorAll('.filter-section a').forEach(el => {
        el.classList.remove('active');
    });

    // 3. 로직 처리
    if (type === 'all') {
        Object.values(sections).forEach(sec => {
            if(sec) sec.classList.remove('hidden');
        });
    } else {
        Object.keys(sections).forEach(key => {
            if (key === type) {
                if(sections[key]) sections[key].classList.remove('hidden');
            } else {
                if(sections[key]) sections[key].classList.add('hidden');
            }
        });
    }

    // 4. 클릭된 탭에 active 클래스 추가
    if (event) event.target.classList.add('active');
}

document.addEventListener("DOMContentLoaded", () => {

    const openBtn = document.getElementById('openMemoBtn');
                    const closeBtn = document.getElementById('closeMemoBtn');
                    const memoWindow = document.getElementById('side-memo');
                    const memoInput = document.getElementById('memoInput');

                    if (openBtn && closeBtn && memoWindow) {
                        // 1. 열기 버튼 클릭
                        openBtn.onclick = () => {
                            memoWindow.classList.add('active');
                        };

                        // 2. 닫기 버튼 클릭
                        closeBtn.onclick = () => {
                            memoWindow.classList.remove('active');
                        };

                        // 3. 로컬 스토리지 연동 (기존과 동일)
                        memoInput.value = localStorage.getItem('userMemo') || '';
                        memoInput.oninput = () => {
                            localStorage.setItem('userMemo', memoInput.value);
                        };
                    }

});

