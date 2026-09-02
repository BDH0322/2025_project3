const id = document.querySelector('meta[name="id"]') ? document.querySelector('meta[name="id"]').getAttribute('content') : '';

    document.addEventListener('DOMContentLoaded', () => {
        // 게시글 좋아요
        document.querySelectorAll('.LikeButton button').forEach(button => {
            button.addEventListener('click', async (e) => {
                const postId = e.target.getAttribute('voteLike');
                if (!postId) return;
                const response = await fetch('/community/like?communityContentNumber=' + postId + '&id=' + id, { method: 'POST' });
                if (response.ok) location.reload();
            });
        });

        // 게시글 싫어요
        document.querySelectorAll('.DisLikeButton button').forEach(button => {
            button.addEventListener('click', async (e) => {
                const postId = e.target.getAttribute('voteDisLike');
                if (!postId) return;
                const response = await fetch('/community/dislike?communityContentNumber=' + postId + '&id=' + id, { method: 'POST' });
                if (response.ok) location.reload();
            });
        });

        // 댓글/답글 수정 토글 & 수정 저장/취소
        document.querySelectorAll('.comment-item, .sub-reply-item').forEach(commentItem => {
            const modifyBtn = commentItem.querySelector('.reply-modify');
            const modifyArea = commentItem.querySelector('.reply-modify-area');
            const replyContentDiv = commentItem.querySelector('.comment-content');

            if (modifyArea) {
                const modifyInput = modifyArea.querySelector('.reply-modify-input');
                const modifySave = modifyArea.querySelector('.reply-modify-save');
                const modifyCancel = modifyArea.querySelector('.reply-modify-cancel');
                const replyNumberInput = commentItem.querySelector('.reply-number');
                const replyNumber = replyNumberInput ? replyNumberInput.value : null;

                if (modifyBtn) {
                    modifyBtn.addEventListener('click', () => {
                        modifyInput.value = replyContentDiv ? replyContentDiv.textContent.trim() : '';
                        modifyArea.style.display = 'block';
                        if (replyContentDiv) replyContentDiv.style.display = 'none';
                        modifyBtn.style.display = 'none';
                    });
                }

                if (modifyCancel) {
                    modifyCancel.addEventListener('click', () => {
                        modifyArea.style.display = 'none';
                        if (replyContentDiv) replyContentDiv.style.display = 'block';
                        if (modifyBtn) modifyBtn.style.display = 'inline-block';
                    });
                }

                if (modifySave) {
                    modifySave.addEventListener('click', async () => {
                        const modifyContent = modifyInput.value;
                        try {
                            const formData = new URLSearchParams();
                            formData.append('replyNumber', replyNumber);
                            formData.append('replyContent', modifyContent);

                            const response = await fetch('/community/replymodify', {
                                method: 'POST',
                                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                                body: formData.toString()
                            });

                            if (response.ok) {
                                if (replyContentDiv) replyContentDiv.textContent = modifyContent;
                                if (replyContentDiv) replyContentDiv.style.display = 'block';
                                modifyArea.style.display = 'none';
                                if (modifyBtn) modifyBtn.style.display = 'inline-block';
                            } else {
                                alert('댓글 수정 실패');
                            }
                        } catch (err) {
                            console.error(err);
                            alert('서버 오류 발생');
                        }
                    });
                }
            }
        });

        // 답글 버튼 토글(입력 영역 표시)
        document.querySelectorAll('.reply-reply').forEach(btn => {
            btn.addEventListener('click', e => {
                const commentItem = e.target.closest('.comment-item');
                const targetArea = commentItem ? commentItem.querySelector('.reply-reply-area') : null;
                if (targetArea) targetArea.style.display = 'block';
            });
        });

        document.querySelectorAll('.reply-reply-cancel').forEach(btn => {
            btn.addEventListener('click', e => {
                const area = e.target.closest('.reply-reply-area');
                if (area) area.style.display = 'none';
            });
        });

        // 댓글 좋아요/싫어요
        document.querySelectorAll('.reply-actions-group button').forEach(button => {
            button.addEventListener('click', async (e) => {
                const action = e.target.getAttribute('data-action');
                const replyNumber = e.target.getAttribute('data-reply');
                if (!action || !replyNumber) return;

                let url = '';
                if (action === 'like') url = '/community/replylike';
                else if (action === 'dislike') url = '/community/replydislike';
                else return;

                const response = await fetch(`${url}?replyNumber=${replyNumber}&id=${id}`, { method: 'POST' });
                if (response.ok) location.reload();
                else alert('투표 실패');
            });
        });
    });

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

