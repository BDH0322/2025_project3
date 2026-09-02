document.addEventListener('DOMContentLoaded', () => {

    // --- 1. 상단 이동 버튼 ---
    const initBackToTop = () => {
        const topBtn = document.getElementById("goToTop");
        if (!topBtn) return;

        window.addEventListener('scroll', () => {
            const scrollTop = window.scrollY || document.documentElement.scrollTop;
            if (scrollTop > 200) {
                topBtn.style.display = "flex";
            } else {
                topBtn.style.display = "none";
            }
        });

        topBtn.addEventListener('click', (e) => {
            e.preventDefault();
            window.scrollTo({ top: 0, behavior: 'smooth' });
        });
    };

    // --- 2. 사이드 메모장 ---
    const openBtn = document.getElementById('openMemoBtn');
    const closeBtn = document.getElementById('closeMemoBtn');
    const memoWindow = document.getElementById('side-memo');
    const memoInput = document.getElementById('memoInput');

        if (openBtn && closeBtn && memoWindow) {
            openBtn.onclick = () => {
                memoWindow.classList.add('active');
            };

            closeBtn.onclick = () => {
                memoWindow.classList.remove('active');
            };

            memoInput.value = localStorage.getItem('userMemo') || '';
            memoInput.oninput = () => {
                localStorage.setItem('userMemo', memoInput.value);
            };
        }
    initBackToTop();
});

