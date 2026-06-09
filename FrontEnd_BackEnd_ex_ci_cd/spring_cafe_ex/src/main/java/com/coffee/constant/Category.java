package com.coffee.constant;

// 상품의 카테고리 정보를 위한 열거형 상수
// 한글 이름도 같이 명시함
public enum Category {
    // 각각 독립된 static 객체
    // 괄호 안에 있는 내용을 생성자의 매개변수로 넣음
    // 1) ALL("전체")를 호출하면, (Category category라고 하면 그냥 선언된거임)
    // 2) 아래에 작성하신 Category(String description)라는 생성자가 실행됩니다.
    // 3) 전달된 "전체"라는 값이 this.description에 저장되는 것이죠.
    // 4) getDescription()으로 다른 곳에서도 쓸 수 있게 하기
    ALL("전체"), BREAD("빵"), BEVERAGE("음료수"), CAKE("케이크"), MACARON("마카롱") ;

    // 매개변수가 문자열이니까 String으로 지정
    // 그냥 맴버변수 만든거임
    private String description ;

    Category(String description) { // 생성자
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}