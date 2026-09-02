document.addEventListener("DOMContentLoaded", () => {
        // 모든 사용자 ID(td) 클릭 시 팝업 열기
        const userCells = document.querySelectorAll(".board-cell-id");

        userCells.forEach(td => {
            const popup = td.querySelector(".user-popup");

            // [수정 포인트 1] 닉네임 가져오기 로직 개선 (어드민/일반 구분 없이 가져오기)
                    // 팝업 내부의 h3 태그에 이미 순수 ID가 있으므로 거기서 가져오는 것이 가장 안전합니다.
            const usernameTag = popup.querySelector("h3");
            const username = usernameTag ? usernameTag.textContent.trim() : "";

            // [수정 포인트 2] td 클릭 이벤트 핸들러
            td.addEventListener("click", (event) => {
                // 게시글 상세 페이지로 이동하는 상위 tr의 이벤트를 막습니다.
                event.stopPropagation();

                // 다른 열린 팝업이 있다면 닫기 (선택사항, 필요 없으면 삭제 가능)
                document.querySelectorAll('.user-popup').forEach(p => {
                    if(p !== popup) p.style.display = 'none';
                });

                popup.style.display = "block";
            });

            // 닫기 버튼 클릭
            const btnClose = popup.querySelector(".btn-close-popup");
            btnClose.addEventListener("click", (event) => {
                event.stopPropagation();
                popup.style.display = "none";
            });

            // 작성글 보기
            const btnView = popup.querySelector(".btn-view-post");
            btnView.addEventListener("click", (event) => {
                event.stopPropagation();
                window.location.href = `/community/user?userId=${username}`;
            });

            // 글쓰기 제한 (BAN)
            const btnBan = popup.querySelector(".btn-ban-user");
            btnBan.addEventListener("click", (event) => {
                event.stopPropagation();
                const banHours = prompt("몇 시간 동안 글쓰기를 금지할까요? (1 = 1일, 10 = 10일)");
                if (!banHours || banHours.trim() === "") return;

                fetch("/community/banUser", {
                    method: "POST",
                    headers: {"Content-Type": "application/x-www-form-urlencoded"},
                    body: `userId=${encodeURIComponent(username)}&hours=${encodeURIComponent(banHours)}`
                })
                .then(r => r.text())
                .then(res => {
                    if (res === "SUCCESS") alert("적용 완료");
                    else if (res === "NO_PERMISSION") alert("권한 없음");
                    else alert("오류 발생");
                });
            });
        });

        // 팝업 외부 클릭 시 닫기
        document.addEventListener("click", (event) => {
            userCells.forEach(td => {
                const popup = td.querySelector(".user-popup");
                if (!popup.contains(event.target)) {
                    popup.style.display = "none";
                }
            });
        });

        /* 사이드 메모 */
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