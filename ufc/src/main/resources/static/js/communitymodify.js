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

