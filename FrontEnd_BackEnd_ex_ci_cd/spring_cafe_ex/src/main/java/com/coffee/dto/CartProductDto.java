package com.coffee.dto;

import lombok.Getter;
import lombok.Setter;

// -> dto는 프론트엔드에서 받는 그릇이면서 service에 넘길 그릇이여서
// controller로 들어올때는 memberId의 값이 없어서 null이지만 결국 데이터베이스로 이동해야하기때문에
// 일단 controller 단계에서는 null값이지만 service를 거쳐 데이터 베이스로 이동될때는 memberId를 채워서 넣기때문에
// 일단 변수를 만들어 놓은 것임
// -> 따라서 프론트엔드에서 받을 파라미터와 반드시 100% 같은 갯수의 변수가 있어야 하는 것은 아니다.
// but! 프론트에서 줄 파라미터를 받으려면 변수명이 같아야 하긴 함
// * 변수명은 같아야 하고 /  변수 갯수는 달라도 됨 *

// 근데 사실상 Member findByEmail (String email); 이 함수로 인해서 email을 알면
// 그 email인 member의 id를 알 수 있어서 굳이 dto에 필요는 없을듯
@Getter @Setter
public class CartProductDto {
    private Long memberId ;
    private Long productId ;
    private int quantity ;
}