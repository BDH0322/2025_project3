document.addEventListener('DOMContentLoaded', function() {
    // --------------------------------------------------------
    // I. 변수 선언 (HTML ID/Class에 맞게 수정)
    // --------------------------------------------------------
    const loginTab = document.getElementById('loginTab');
    const registerTab = document.getElementById('registerTab');
    const loginForm = document.querySelector('.login-form');
    const registerForm = document.querySelector('.register-form'); // 폼 제출 이벤트 연결 대상
    
    // 아이디 관련
    const idInput = document.getElementById('registerIdInput'); // 🌟🌟 registerIdInput으로 수정 🌟🌟
    const idCheckMessage = document.getElementById('idCheckMessage');
    const idCheckButton = document.getElementById('idCheckButton');
    
    // 비밀번호 관련 (HTML에 ID가 추가되었다고 가정)
    const passwordInput = document.getElementById('passwordInput');
    const passwordConfirmInput = document.getElementById('passwordConfirmInput');
    const passwordMessage = document.getElementById('passwordMessage');
    const passwordConfirmMessage = document.getElementById('passwordConfirmMessage');
    
    // 버튼
    const registerSubmitButton = document.querySelector('#registerForm .submit-button');
    
    // 상태 변수
    let isIdAvailable = false;
    let isPasswordValid = false;
    let isPasswordConfirmValid = false;
    
    // hasMemberDTO 참조 오류 방지용 변수 (JS 하단 로직용)
    const urlParams = new URLSearchParams(window.location.search);
    const hasErrorMessage = urlParams.has('errorMessage');
    const hasMemberDTO = false;

    // --------------------------------------------------------
    // II. 탭 전환 기능 (수정 없음)
    // --------------------------------------------------------
    loginTab.addEventListener('click',function(){
        loginTab.classList.add('active');
        registerTab.classList.remove('active');
        loginForm.classList.add('active');
        registerForm.classList.remove('active');
    });

    registerTab.addEventListener('click',function(){
        registerTab.classList.add('active');
        loginTab.classList.remove('active');
        registerForm.classList.add('active');
        loginForm.classList.remove('active');
    });

    // --------------------------------------------------------
    // III. 아이디 관련 기능
    // --------------------------------------------------------
    if (idInput && registerSubmitButton) {
        // A. 아이디 입력 변경 시 초기화
        idInput.addEventListener('input', function() {
            isIdAvailable = false;
            registerSubmitButton.disabled = true;
            idCheckMessage.textContent = '아이디 중복 확인이 필요합니다.';
            idCheckMessage.style.color = 'red';
        });

        // B. 아이디 중복 확인 기능 (GET 요청)
        idCheckButton.addEventListener('click', function() {
            const userId = idInput.value;
            if (userId.length < 4) {
                idCheckMessage.textContent = "아이디는 4자 이상이어야 합니다.";
                idCheckMessage.style.color = 'red';
                isIdAvailable = false;
                registerSubmitButton.disabled = true;
                return;
            }
            fetch('/checkUserId?id=' + encodeURIComponent(userId), {
                method: 'GET',
            })
            .then(response => response.text())
            .then(data => {
                const trimmedData = data.trim();
                if (trimmedData === 'duplicate') {
                    idCheckMessage.textContent = "이미 사용 중인 아이디입니다.";
                    idCheckMessage.style.color = "red";
                    isIdAvailable = false;
                } else if (trimmedData === 'available') {
                    idCheckMessage.textContent = "사용 가능한 아이디입니다.";
                    idCheckMessage.style.color = "green";
                    isIdAvailable = true;
                    // 아이디 확인이 끝났더라도 최종 제출 버튼 활성화는 모든 검사 후에 이루어집니다.
                } else {
                    console.error('Unexpected server response:', data);
                    idCheckMessage.textContent = "아이디 중복 확인 중 오류가 발생했습니다.";
                    idCheckMessage.style.color = "red";
                    isIdAvailable = false;
                }
                updateSubmitButtonState(); // 상태 업데이트 후 버튼 상태 변경
            })
            .catch(error => {
                console.error('Error', error);
                idCheckMessage.textContent = "서버 통신 오류가 발생했습니다.";
                idCheckMessage.style.color = "red";
                isIdAvailable = false;
                updateSubmitButtonState();
            });
        });
    }

    // --------------------------------------------------------
    // IV. 비밀번호 유효성 검사 및 일치 확인 기능 (🌟핵심 추가 로직🌟)
    // --------------------------------------------------------
    if (passwordInput && passwordConfirmInput) {
        
        function validatePassword() {
            const pwd = passwordInput.value;
            const pwdConfirm = passwordConfirmInput.value;
            let valid = true;
            let messages = [];

            // 1. 길이 검사
            if (pwd.length < 4) {
                messages.push('4자 이상');
                valid = false;
            }
            // 2. 특수 문자 검사 (추가된 로직)
            // 특수 문자 정규식: !@#$%^&*()_+={}\[\]:;"'<>,.?/~`
            const specialCharRegex = /[!@#$%^&*()_+={}[\]:;"'<>,.?/~`]/; 
            if (!specialCharRegex.test(pwd)) {
                messages.push('특수문자 1개 이상 (@$!%*?& 등)');
                valid = false;
            }

            // 유효성 메시지 업데이트
            if (!valid) {
                passwordMessage.textContent = '비밀번호는 ' + messages.join(', ') + '를 포함해야 합니다.';
                passwordMessage.style.color = 'red';
                isPasswordValid = false;
            } else {
                passwordMessage.textContent = '유효한 비밀번호입니다.';
                passwordMessage.style.color = 'green';
                isPasswordValid = true;
            }

            // 3. 비밀번호 일치 확인
            if (pwdConfirm === "") {
                passwordConfirmMessage.textContent = '비밀번호 확인을 입력해 주세요.';
                passwordConfirmMessage.style.color = 'red';
                isPasswordConfirmValid = false;
            } else if (pwd === pwdConfirm) {
                passwordConfirmMessage.textContent = '비밀번호가 일치합니다.';
                passwordConfirmMessage.style.color = 'green';
                isPasswordConfirmValid = true;
            } else {
                passwordConfirmMessage.textContent = '비밀번호가 일치하지 않습니다.';
                passwordConfirmMessage.style.color = 'red';
                isPasswordConfirmValid = false;
            }

            updateSubmitButtonState(); // 비밀번호 상태 변경 후 버튼 상태 변경
        }

        // 이벤트 리스너 연결
        passwordInput.addEventListener('input', validatePassword);
        passwordConfirmInput.addEventListener('input', validatePassword);
    }


    // --------------------------------------------------------
    // V. 제출 버튼 활성화/비활성화 함수
    // --------------------------------------------------------
    function updateSubmitButtonState() {
        if (registerSubmitButton) {
            if (isIdAvailable && isPasswordValid && isPasswordConfirmValid) {
                registerSubmitButton.disabled = false;
            } else {
                registerSubmitButton.disabled = true;
            }
        }
    }


    // --------------------------------------------------------
    // VI. 폼 제출 시 최종 확인 (Submit Event Target 수정)
    // --------------------------------------------------------
    if (registerForm) {
        // 🌟 submit 이벤트는 폼 요소(registerForm)에 연결해야 합니다. 🌟
        registerForm.addEventListener('submit', function(e) {

            // 폼 필드가 비어있는지 확인 (HTML required 속성으로도 처리되지만 JS에서 한 번 더 확인)
            const requiredFields = registerForm.querySelectorAll('[required]');
            let allFieldsFilled = true;
            requiredFields.forEach(field => {
                if (field.value.trim() === '') {
                    allFieldsFilled = false;
                }
            });

            if (!allFieldsFilled) {
                alert('모든 필수 입력란을 채워주세요.');
                //e.preventDefault();
                return;
            }

            if (!isIdAvailable || !isPasswordValid || !isPasswordConfirmValid) {
                alert("회원가입 정보를 정확히 확인해주세요 (아이디 중복 확인, 비밀번호 규칙/일치).");
                //e.preventDefault();
            }
        });
    }
    // --------------------------------------------------------
    // VII. 초기 폼 활성화 및 URL 파라미터 처리
    // --------------------------------------------------------
    // --------------------------------------------------------
    registerForm.classList.remove('active');
    loginForm.classList.add('active');
    updateSubmitButtonState(); // 초기에는 버튼 비활성화

    if (hasErrorMessage || hasMemberDTO) {
        registerTab.click(); // 회원가입 실패 메시지 있으면 회원가입 탭으로 이동
    } else if (urlParams.has('successMessage') || urlParams.has('error')) {
        loginTab.click(); // 로그인 관련 메시지 있으면 로그인 탭으로 이동
    }
});