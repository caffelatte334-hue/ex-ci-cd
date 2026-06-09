package com.coffee.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// 1) WebConfig 클래스를 @Configuration 어노테이션에 의하여 Spring Boot의 설정 클래스로 등록
@Configuration // 설정용 파일 어노테이션
// 2) WebMvcConfigurer를 상속받기
public class WebConfig implements WebMvcConfigurer {
    /*@Override // 3) addCorsMappings 메소드 오버라이딩하기
    public void addCorsMappings(CorsRegistry registry) {
        // 3000번 포트에서 GET부터 PATCH까지의 열거한 요청들을 모두 수락하겠습니다.
        registry.addMapping("/**") *//* 모든 경로 허용  *//* // 모든 URL 허용
                *//* react 포트 *//*
                .allowedOrigins("http://localhost:5173", "http://localhost:3000")
                *//* 허용할 메핑 *//*
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                // 쿠키 전송 허용
                .allowCredentials(true) ;
    }*/

    // 이미지 가져오는 법
    // @Value는 값을 자바 변수로 가져오는 어노테이션 / 가져오는 경로는 기본적으로 설정파일 (application.properties)
    // 설정 파일에서 uploadPath라는 key가 가진 값을 가져와서 아래의 변수에 값으로 넣어라
    // ${ }의 의미 : 스프링(Spring) 프레임워크 전반에서는 "어딘가에 저장된 값을 꺼내올 때 쓰는 공통 약속"
    @Value("${uploadPath}")
    private String uploadPath ; // file:///C:/shop/images/

    // 정적 리소스 핸들러(Static Resource Handler)
    // 웹 브라우저의 가상 주소와 컴퓨터의 실제 폴더를 연결하는 다리 역할하는 메소드
    // Override from WebMvcConfigurer
    @Override // addResourceHandlers : 리소스를 어디서 어떻게 가져올지 규칙을 정하는 메서드 이름
    public void addResourceHandlers(ResourceHandlerRegistry registry) { // 등록 대장 객체
        registry // 웹 브라우저의 가상 주소
                // 리액트에서 JSX영역에 해당 소스로 이미지를 요청하면 그 요청을 수락하고
                // 요청 받은 그 이미지를 .addResourceLocations(uploadPath);로 인해
                // uploadPath(application.properties파일에 있는 변수)라는 변수를 통해 운영체제에 해당 위치에 있는
                // 이미지 파일을 가져옴
                .addResourceHandler("/images/**") //**은 이 주소안의 모든 것을 의미
                // *은 현재 위치 바로 아래 모든것 (/images/apple.png 가능) (/images/fruit/apple.png 불가능)
                // **은 하위 폴더를 포함한 모든 것 (/images/apple.png 가능) (/images/fruit/apple.png 가능)

                // 컴퓨터의 실제 폴더 위치
                // @Value 어노테이션에 의해 uploadPath라는 변수를 사용할 수 있음
                .addResourceLocations(uploadPath);

        /* 프로그램 진행 순서 (중요!!!)
        1) <img src="/images/latte.jpg">라는 코드를 실행
        2) 브라우저: "스프링 서버야, /images/latte.jpg 파일 좀 줘!
        3) 스프링: "잠깐만, 내 등록 대장(registry)을 확인해 볼게.
        어? /images/** 패턴은 uploadPath로 가라고 되어 있네?"
        4) 서버 시스템: "알겠어. 실제 위치인 C:/shop/images/
        폴더에 가서 latte.jpg가 있는지 찾아볼게."
        5) 결과: 파일을 찾아서 브라우저에 이미지를 띄워줍니다.
        */
    }
}