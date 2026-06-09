package com.coffee.dto;

import lombok.Getter;
import lombok.Setter;

// dto(Data Transfer Object)
// 클라이언트에서 넘겨진 로그인 정보를 저장하기 위한 자바 클래스
@Getter @Setter
public class LoginDto { // 리액트에서 보내는 파라미터와 Key 이름을 동일하게 하기
    private String email ;
    private String password ;
}