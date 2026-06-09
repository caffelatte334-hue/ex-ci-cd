package com.coffee.config;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// 컨트롤러마다 매번 로그인 확인할 필요없이 컨트롤러 전에 공통적으로 로그인 확인하는 자동화 시스템
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // 아직 무의미한 null값임 - 값을 주입해야함
    // 사용할 도구 가져오기 (마치 Repository와 Service의 관계)
    private final JwtTokenProvider jwtTokenProvider ;

    // 값을 주입함 (@RequiredArgsConstructor 이 어노테이션으로 해결해도 됨)
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider){
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override // 모든 요청이 들어올 때 컨트롤러 보다 먼저 (한번만!) 실행이 되는 핵심 로직(메소드)
    protected void doFilterInternal(
            HttpServletRequest request, // 프론트가 들고 온 요청 정보
            HttpServletResponse response, // 프론트에게 돌려줄 응답 정보
            // 검문소 다음 단계로 이어지는 고속도로 통로 (다음 필터들)
            FilterChain filterChain) throws ServletException, IOException {

        // 1단계: 가방 뒤지기 (Authorization 헤더 확인)
        // 프론트가 들고온 요청 헤더에서 Authorization 값을 가져온다
        String bearer = request.getHeader("Authorization");
        // 값이 존재하고 "Bearer "로 시작하는지 확인한다
        if(bearer != null && bearer.startsWith("Bearer ")){
            // Bearer "를 제거하여 JWT 토큰만 추출한다
            String token = bearer.substring("Bearer ".length());

            // 2단계: 출입증 진위 여부 및 신원 파악
            // 토큰의 유효성을 검증한다 (validateToken) (만료여부, 위조여부)
            if(jwtTokenProvider.validateToken(token)){
                // 토큰에서 사용자 이메일을 추출한다 (getEmail)
                // getEmail() : 사실상 email을 가져오는게 아니고 토큰의 claim에서 subject(식별자)를 가져옴
                // 그냥 변수명이랑 메소드명이 email, getEmail임 (실제로 식별자에 email 값을 넣긴했지만)
                String email = jwtTokenProvider.getEmail(token); // 회원의 이메일 정보 가져오기
                Claims claims = jwtTokenProvider.getClaims(token); // 토큰 해체해서 객체에 넣기

                // 토큰의 claims에서 role 값을 추출한다
                String role = claims.get("role", String.class);

                // 3단계: 시큐리티 전용 통행증 발급 및 보고 (★보안의 핵심)
                // 권한 객체 생성 (권한을 담고 있는 객체 모음)
                // 인터페이스라서 객체 생성 불가능 - 구현체를 이용해서 객체를 생성해야 함
                // SimpleGrantedAuthority라는 구현체를 이용함
                // 스프링 시큐리티는 문자열 데이터 그대로를 인식 못해서
                // 이해할 수 있는 양식인 GrantedAuthority로 변환해줌
                List<GrantedAuthority> authorities
                        = List.of(new SimpleGrantedAuthority("ROLE_" + role));

                // 인증 객체 생성 (인증 객체 auth 발행)
                UsernamePasswordAuthenticationToken auth
                        // 비밀번호 자리는 토큰으로 대체해서 null로 표시함
                        = new UsernamePasswordAuthenticationToken(email, null,
                        authorities);

                // SecurityContextHolder : 스프링 시큐리티에 인증 객체인 auth를 넣어두면
                // 스프링 시큐리티가 이 요청을 보낸 사람이 로그인이 완벽히 된 사람이라고 인식함
                SecurityContextHolder.getContext().setAuthentication(auth);

            }
        }

        // 검문 종료: 고속도로 개통 -> 컨트롤러로 패스시킴
        // 출입증이 없는 비로그인 유저라도 컨트롤러로 패스시킴
        // Authentication가 SecurityContext안에 있고 이것을 SecurityContextHolder가 유지보수 및 관리를 해줌
        filterChain.doFilter(request, response);
    }
}