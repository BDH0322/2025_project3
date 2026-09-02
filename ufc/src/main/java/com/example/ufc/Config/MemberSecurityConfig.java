package com.example.ufc.Config;


import com.example.ufc.Service.CustomUserDetailService;
import com.example.ufc.Service.CustomUserDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class MemberSecurityConfig {

    private final CustomUserDetailService customUserDetailsService;


    public MemberSecurityConfig(CustomUserDetailService customUserDetailsService){

        this.customUserDetailsService = customUserDetailsService;
    }

    public CustomUserDetailService getCustomUserDetailsService() {
        return customUserDetailsService;
    }

    // BCryptPasswordEncoder는 Spring Security의 외부 라이브러리 클래스입니다.
    @Bean
    public static BCryptPasswordEncoder memberPasswordEncoder(){
        return new BCryptPasswordEncoder();//이 객체를 Spring 컨테이너에 등록합니다.
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(){
        // 1. DaoAuthenticationProvider 객체 생성
        //    -> 데이터베이스(DAO)를 통해 사용자 정보를 가져와 인증을 처리하는 표준 컴포넌트입니다.
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        // 2. UserDetailsService 설정 (사용자 정보 로드)
        //    -> "인증 시도 시, 사용자 ID를 기반으로 DB에서 정보를 어떻게 가져올지"를 지정합니다.
        //    -> customUserDetailsService는 개발자가 MemberEntity를 UserDetails로 변환하는 로직을 구현한 서비스입니다.
        authProvider.setUserDetailsService(customUserDetailsService);


        // 3. PasswordEncoder 설정 (비밀번호 검증)
        //    -> "사용자가 입력한 비밀번호와 DB에 저장된 암호화된 비밀번호를 어떻게 비교할지"를 지정합니다.
        //    -> memberPasswordEncoder() 메서드(@Bean으로 등록됨)가 반환하는 BCryptPasswordEncoder 등을 사용합니다.
        authProvider.setPasswordEncoder(memberPasswordEncoder());
        // 4. 설정이 완료된 Provider 객체를 Spring 컨테이너에 등록합니다.
        //    -> SecurityFilterChain이 이 Provider를 사용하여 실제 인증(로그인)을 수행합니다.
        return authProvider;
    }


    @Bean
    public SecurityFilterChain memberFilterChain(HttpSecurity http) throws Exception {
        // 1. CSRF 설정: 개발 편의를 위해 비활성화
        http.csrf(csrf -> csrf.disable());

        // 2. 요청별 접근 권한 설정
        http.authorizeHttpRequests(auth -> {
            auth
                    // --------------------------------------------------------------------------------
                    // A. 전체 허용 경로 (permitAll())
                    //    UFC 프로젝트의 모든 HTML 페이지와 로그인/회원가입 관련 요청을 허용합니다.
                    // --------------------------------------------------------------------------------
                    .requestMatchers(
                            // 현재 존재하는 HTML 경로들
                            "/", "/main", "/rank", "/login","/trend","/integratedSearch",
                            "/fighter/{name}",
                            "/checkUserId","/membersave","/loginprocess",
                            "/predictAI",
                            "/predict/**",
                            "/vote/**","/community/**", "/communityimages/**",
                            // 정적 리소스 허용
                            "/css/**", "/js/**", "/images/**"
                    ).permitAll()

                    // --------------------------------------------------------------------------------
                    // B. 로그인 사용자만 허용 (Authenticated)
                    //    현재는 회원 전용 기능이 없으므로, 향후 "마이페이지"나 "글쓰기" 등을 위해 남겨둡니다.
                    // --------------------------------------------------------------------------------
                    /*
                    .requestMatchers(
                             // 예시: 마이페이지는 로그인해야만 접근 가능
                    ).authenticated()
                    */
                    // --------------------------------------------------------------------------------
                    // C. 그 외 모든 요청
                    // --------------------------------------------------------------------------------
                    // 명시적으로 허용되지 않은 나머지 모든 경로는 로그인(인증)이 필요합니다.
                    .anyRequest().authenticated();
        });

        // 3. 폼 로그인 설정
        http.formLogin(formLogin -> {
            formLogin
                    .loginPage("/login")             // 로그인 폼 페이지
                    .loginProcessingUrl("/loginprocess")
                    .usernameParameter("loginId")
                    .passwordParameter("loginPw")
                    .successHandler((request, response, authentication) -> {
                        // 일반 회원 로그인 성공 처리
                        if (authentication.getPrincipal() instanceof CustomUserDetails customUser) {
                            request.getSession().setAttribute("loginstate", true);
                            request.getSession().setAttribute("id", customUser.getId());

                            request.getSession().setAttribute("admin",customUser.getAdmin());
                        }
                        response.sendRedirect("/main"); // 로그인 후 메인으로 이동
                    })
                    .failureHandler((request, response, exception) -> {
                        response.sendRedirect("/login?error=true");
                    })
                    .permitAll();
        });

        // 4. 로그아웃 설정
        http.logout(logout -> {
            logout
                    .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                    .logoutSuccessUrl("/main")
                    .deleteCookies("JSESSIONID")
                    .invalidateHttpSession(true)
                    .clearAuthentication(true)
                    .permitAll();
        });

        // 5. 인증 제공자 등록
        http.authenticationProvider(authenticationProvider());

        return http.build();
    }


}
