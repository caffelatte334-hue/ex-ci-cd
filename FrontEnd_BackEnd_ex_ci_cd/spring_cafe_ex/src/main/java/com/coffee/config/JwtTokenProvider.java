package com.coffee.config;

import com.coffee.entity.Member;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Component // bean 객체를 만들어서 관리 - 나중에 컨트롤러에서 편하게 불러다 쓸 수 있음
public class JwtTokenProvider { // JWT 생성, 검증 기능 담당자 클래스
    // 노출시 해커가 마음대로 토큰 위조 가능
    // 실무에서는 application.properties에 숨겨둠 (@Value로 설정해서 가져오기)
    // 토큰을 위조하지 못하도록 도장을 찍을 때 사용할 백엔드만의 비밀번호(비밀키)

    @Value("${jwt.secret}")
    private String secretKey;

    // 토큰 유효 기간 설정 (밀리초(ms) 단위)
    @Value("${jwt.expiration}")
    private long expiration; // 만료 1 시간

    // 우리가 지정한 문자열 SECRET_KEY를 JWT 라이브러리가 서명용 도장으로 인식할 수 있도록
    // 컴퓨터용 암호화 키 객체로 다듬어서 반환해 주는 내부 헬퍼 메서드
    private Key signingKey;
    @PostConstruct
    protected void init() {
        this.signingKey = Keys.hmacShaKeyFor(secretKey.getBytes());
    }
    private Key getSigningKey() {
        return signingKey; // 위조 방지를 위한 서명
    }


    // 1. 토큰(JWT) 생성 : createToken(Member member) 메소드
    // 회원 정보를 가지고 그에 해당하는 토큰 생성
    // MemberController 클래스에서 인증 성공한 사용자를 위하여 로그인 증명서(토큰)를 발급하는 데 사용될 예정입니다.
    public String createToken(Member member){ // 매개 변수 : 토큰 안에 사용자 식별값 저장
        return Jwts.builder()
                .setSubject(member.getEmail()) // 토큰 주인 - sub 토큰 주제(Subject, 보통 사용자 ID)
                .setIssuedAt(new Date()) // 토큰 발급 시간 - iat 발급 시간(Issued At)
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // 토큰 만료 시간 - exp 만료 시간(Expiration)
                // 비밀키 도장(getSigningKey())과 HS256이라는 암호화 알고리즘을 사용해서 토큰 맨 뒤에 절대 위조할 수 없는 전자 서명(Sign)을 각인
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                // 표준 클레임 말고 우리가 커스텀하게 넣고 싶은 정보(예: "role": "USER")를 Map 형태로 추가 주입
                .claim("role", member.getRole().name()) // 권한 정보
                // 이 모든 정보를 압축하여 점(.) 두 개로 연결된 그 유명한 JWT 긴 문자열(eyJhbGci...)을 완성
                .compact(); // 최종 문자열 생성하기
    }

    // 2. 토큰 해체 및 정보 추출
    // 외부에서 토큰을 주면, 그 토큰을 해체해서 안에 들어있던 주인공 이메일(Subject)만 쏙 뽑아서 돌려주는 편의 메서드
    public String getEmail(String token){ // JWT 토큰에서 사용자 정보 가져 오기
        return this.getClaims(token).getSubject() ; // .setSubject(member.getEmail())
    }
    // 토큰 해체 분석기
    public Claims getClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 3. 출입증 위조/만료 검사
    // 들어온 토큰을 우리 비밀키로 시험 삼아 파싱(해체)해 보는 검문소
    // 온전하게 해체가 잘 되면 법적으로 유효한 토큰이므로 true를 뱉고 통과
    public boolean validateToken(String token){ // JWT 토큰 유효성 검사
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true; // 아무 문제 없으면 정상적인 토큰이므로 true 반환!

        } catch (ExpiredJwtException e) { // 발급된 지 1시간이 넘어서 유통기한이 지난 토큰일 때
            System.out.println("토큰 만료됨");

        // 해커가 글자를 임의로 바꿨거나 서명이 일치하지 않는 가짜 위조 토큰일 때
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            System.out.println("토큰 서명/형식 오류");

        } catch (Exception e) { // 그외 예외 사항
            System.out.println("기타 토큰 오류");
        }
        return false ;
    }

}