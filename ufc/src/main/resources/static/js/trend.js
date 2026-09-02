document.addEventListener('DOMContentLoaded', () => {

    // ==========================================
    // 1. 통계 슬라이더 (Statistic Slider)
    // ==========================================
    const initStatisticSlider = () => {
        const sliderContent = document.querySelector('.statistic-container');
        const prevButton = document.getElementById('prevSlide');
        const nextButton = document.getElementById('nextSlide');
        const dotsContainer = document.getElementById('StatisticDots');
        const totalSlides = 3;
        let currentSlide = 0;
        let slideInterval;

        // 도트 생성 함수
        const createDots = () => {
            if (!dotsContainer) return;
            for (let i = 0; i < totalSlides; i++) {
                const dot = document.createElement('button');
                dot.classList.add('dot');
                dot.dataset.index = i;
                dot.addEventListener('click', () => {
                    currentSlide = i;
                    updateSlider();
                    resetAutoSlide();
                });
                dotsContainer.appendChild(dot);
            }
        };

        // 슬라이더 업데이트 함수
        const updateSlider = () => {
            const offset = currentSlide * -33.333;
            if (sliderContent) sliderContent.style.transform = `translateX(${offset}%)`;

            const dots = dotsContainer?.querySelectorAll('.dot');
            dots?.forEach((dot, index) => {
                dot.classList.toggle('active', index === currentSlide);
            });
        };

        const nextSlide = () => {
            currentSlide = (currentSlide + 1) % totalSlides;
            updateSlider();
        };

        const prevSlide = () => {
            currentSlide = (currentSlide - 1 + totalSlides) % totalSlides;
            updateSlider();
        };

        const startAutoSlide = () => {
            slideInterval = setInterval(nextSlide, 5000);
        };

        const resetAutoSlide = () => {
            clearInterval(slideInterval);
            startAutoSlide();
        };

        // 이벤트 리스너 등록
        nextButton?.addEventListener('click', () => { nextSlide(); resetAutoSlide(); });
        prevButton?.addEventListener('click', () => { prevSlide(); resetAutoSlide(); });

        // 초기 실행
        createDots();
        updateSlider();
        startAutoSlide();
    };

    // ==========================================
    // 2. 핫 파이터 슬라이더 (Hot Fighter Slider)
    // ==========================================
    const initHotFighterSlider = () => {
        const hotSliderContent = document.querySelector('.slide-page-bundle');
        const hotPrevButton = document.querySelector('.hot-prev-button');
        const hotNextButton = document.querySelector('.hot-next-button');
        const hotDotsContainer = document.getElementById('HotDot');
        const hotTotalSlides = 3;
        let hotCurrentSlide = 0;
        let hotSlideInterval;

        const hotCreateDots = () => {
            if (!hotDotsContainer) return;
            for (let i = 0; i < hotTotalSlides; i++) {
                const dot = document.createElement('button');
                dot.classList.add('dot');
                dot.addEventListener('click', () => {
                    hotCurrentSlide = i;
                    hotUpdateSlider();
                    hotResetAutoSlide();
                });
                hotDotsContainer.appendChild(dot);
            }
        };

        const hotUpdateSlider = () => {
            const offset = hotCurrentSlide * -33.333;
            if (hotSliderContent) hotSliderContent.style.transform = `translateX(${offset}%)`;

            const dots = hotDotsContainer?.querySelectorAll('.dot');
            dots?.forEach((dot, index) => {
                dot.classList.toggle('active', index === hotCurrentSlide);
            });
        };

        const hotNextSlide = () => {
            hotCurrentSlide = (hotCurrentSlide + 1) % hotTotalSlides;
            hotUpdateSlider();
        };

        const hotPrevSlide = () => {
            hotCurrentSlide = (hotCurrentSlide - 1 + hotTotalSlides) % hotTotalSlides;
            hotUpdateSlider();
        };

        const hotStartAutoSlide = () => {
            hotSlideInterval = setInterval(hotNextSlide, 5000);
        };

        const hotResetAutoSlide = () => {
            clearInterval(hotSlideInterval);
            hotStartAutoSlide();
        };

        hotNextButton?.addEventListener('click', () => { hotNextSlide(); hotResetAutoSlide(); });
        hotPrevButton?.addEventListener('click', () => { hotPrevSlide(); hotResetAutoSlide(); });

        hotCreateDots();
        hotUpdateSlider();
        hotStartAutoSlide();
    };

    // ==========================================
    // 3. 상단 이동 버튼 (Go To Top)
    // ==========================================
    const initBackToTop = () => {
        const topBtn = document.getElementById("goToTop");
        if (!topBtn) return;

        // window.onscroll 대신 addEventListener 사용
        window.addEventListener('scroll', () => {
            // 브라우저 호환성을 위해 두 가지 스크롤 측정값 사용
            const scrollTop = window.scrollY || document.documentElement.scrollTop;

            if (scrollTop > 200) {
                topBtn.style.display = "flex"; // CSS 중앙정렬(flex)을 유지하기 위해 flex 사용
            } else {
                topBtn.style.display = "none";
            }
        });

        topBtn.addEventListener('click', (e) => {
            e.preventDefault(); // a 태그의 기본 동작 방지
            window.scrollTo({
                top: 0,
                behavior: 'smooth'
            });
        });
    };

    // ------------------------------------------
    // 모든 기능 방(함수) 실행
    // ------------------------------------------
    initStatisticSlider();
    initHotFighterSlider();
    initBackToTop();

    // 사이드 메모장
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