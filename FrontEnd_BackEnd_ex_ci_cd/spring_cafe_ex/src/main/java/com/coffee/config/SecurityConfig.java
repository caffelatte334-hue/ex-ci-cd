package com.coffee.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration // 설정용 파일 어노테이션
@RequiredArgsConstructor
public class SecurityConfig {

    // JwtTokenProvider.java에서 @Component로 생성함
    // 토큰 검독기
    private final JwtTokenProvider jwtTokenProvider;

    // CorsConfig.java에 CorsConfigurationSource의 @Bean으로 객체 생성이 되어 있음
    // 리액트(5173 포트)의 접근을 허락해 줄 교차 출입 허가증 정보
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean // 비밀번호를 암호화하는 메소드 (BCrypt 방식)
    // "admin@naver.com"을 DB에 "$2a$10$JvA36EqRtX4EHtsv42wUXeO6ma9OpdvxO7sZ6rhufqMbTgkclmacW"로 저장함
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 스프링 컨테이너가 관리하는 객체로 설정하는 어노테이션 (Java의 객체를 부르는 이름이 Bean임)
    // 이 어노테이션이 붙으면 그 메소드의 결과물인 반환데이터를 객체화 시켜서
    // 스프링 빈(Spring Bean)이라는 이름으로 스프링 컨테이너에 넣고 관리함
    // 원래 일반적으로 객체를 사용할때는 new연산자를 사용해서 매번 새로 만들지만 이렇게 객체화 시켜서 컨테이너에 저장해놓으면
    // 나중에 해당 객체가 필요할때 새로 생성하지 않고 스프링 컨테이네에서 꺼내다 재사용 할 수 있음
    @Bean
    // SecurityFilterChain : 스프링 시큐리티의 보안 필터들이 순서대로 늘어선 체인(사슬)" 그 자체를 의미하는 인터페이스
    // HttpSecurity : SecurityFilterChain의 필터를 조립하는 도구함
    // throws : 예외 전이 선언문 - 호출한 상위 메서드가 알아서 처리하게 하기 <-> try-catch는 내가 직접 예외사항 처리하기
    // Exception은 모든 예외 상황을 포함하는 최상위 클래스라서 throws Exception은 아주 포괄적인 선언문임
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // 인증 없이 요청을 허용할 url 배열 (배열 생성 초기화 기법)
        String[] permitUrls = {
                "/images/**",
                "/fruit/**",
                "/css/**",
                "/js/**",
                "/member/signup",
                "/member/login",
                "/product/list",
                "/fruit01/**"
        };

        // Spring Security 기본 정책 : POST / PUT / DELETE 요청은 CSRF 토큰 필요함
        http    // CorsConfig에 설정해놓은 반환값인 source를 사용해서 CORS 설정함
                // 주입받은 교차 허가증(corsConfigurationSource)을 시큐리티 시스템에 주입
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                // 사이트 간 요청 위조(CSRF) 방지 기능을 일단 비활성화 시켜 두고 -> (.csrf(csrf -> csrf.disable()))
                // 나중에 JWT를 사용해서 CSRF를 끄고 키고 할 수 있음
                .csrf(csrf -> csrf.disable())
                // 세션(Session)이라는 메모리 카드를 절대 쓰지 않는 STATELESS(무상태) 방식을 채택
                // -> 세션말고 오직 토큰만 검사 (JWT 보안의 핵심 정책!)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 요청에 대한 권한 설정 (인가)
                // .authorizeHttpRequests(auth -> auth ...) : 어떤 주소로 들어오는 요청을 허용하거나 막을지 정하는 구역
                .authorizeHttpRequests(auth -> auth
                        // permitUrls 배열에 담긴 주소들은 누구에게나(Permit All) 허용함
                        .requestMatchers(permitUrls).permitAll()
                        // 그 외의 나머지 모든 요청(anyRequest)은 반드시 로그인(authenticated)을 해야만 볼 수 있게 막음
                        .anyRequest().authenticated()
                );

        // JWT 필터 등록 (JwtAuthenticationFilter에 생성되어있음)
        // 내가 만든 JWT 필터로 검사하기
        http.addFilterBefore(
                new JwtAuthenticationFilter(jwtTokenProvider),
                UsernamePasswordAuthenticationFilter.class
        );

        // http에 넣은 모든 설정(CORS 허용, CSRF 비활성화, 세션 안 쓰기, 하이패스 주소 지정, JWT 경비원 배치 등)을
        // 하나의 거대한 사슬 객체(SecurityFilterChain)로 완성해 반환함
        return http.build();
    }

    @Bean
    // 로그인 처리를 총괄 지휘하는 대빵 매니저 객체인 AuthenticationManager를 스프링 빈으로 등록
    // 나중에 사용자가 아이디/비번을 치고 들어왔을 때 MemberDetailsService를
    // 호출해서 진짜 로그인을 진행시키는 실무 총책임자 역할
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }
}
// 더 이상 필요없어진 설정 (옛날 방식의 서버가 직접 로그인 창 HTML 화면을 띄워주던 방식) - 이제는 리액트가 함
/*// .formLogin(form -> form ...) : 사용자에게 보여줄 로그인 폼에 대한 설정
// 인증 리다이렉션 경로 지정:
// -> 로그인이 필요한 페이지에 권한 없는 사용자가 접근하면, 어느 주소로 보낼지(Redirect) 정하는 것
// 해당 주소로 이동시 어떤 HTML주소를 보여줄지 정하는 컨트롤러와는 다른 개념임
// 따라서 리다이렉션도하고 컨트롤러에서 "/member/login" 에 맞는 HTML도 따로 설정해줘야함
.formLogin(form -> form
        // 스프링이 제공하는 기본 로그인창 대신, 직접 만든 /member/login 주소의 화면을 로그인 페이지로 사용
        .loginPage("/member/login")
        // 로그인 페이지 자체는 로그인을 안 한 사람도 접근할 수 있어야 하므로 접근을 허용
        .permitAll()
);*/

// 이제는 해당 설정을 CorsConfig 클래스로 설정해놓아서 그걸 가져오면 됨
/*        // 다른 도메인(예: 리액트 포트 5173)에서 오는 요청을 허용하기 위한 기초 설정을 적용
// SecurityConfig의 이 설정이 없을때는 다른 오리진에서 들어오는 것을
// WebConfig의 WebMvcConfigurer가 직접 결정하는데
// 이 설정이 생기면 이제부터는 오리진 허용 여부도 SecurityConfig가 관리하고
// WebConfig의 WebMvcConfigurer는 단순히 설정된 값만 SecurityConfig에게 전달하게 됨
// * 사실상 이 코드는 WebMvcConfigurer의 내용 전체를 가져오는 것과 같음 *
// 따로 설정도 가능하지만 공백으로 두어서 WebMvcConfigurer 내용을 가져와서 사용하게 함
http.cors(cors -> {});*/

// 위에서 build() 함
/*// 지금까지 http 도구함에 담은 모든 설정들을 빌드해서
// 실제 동작하는 보안 필터 객체(SecurityFilterChain)를 생성하여 반환
return http.build();*/