package com.coffee.controller;

import com.coffee.entity.Fruit;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class FruitHtmlController {
    // 기본 포트가 "server.port=9000"라고
    // application.properties 파일에 설정해두어서
    // http://localhost:9000/fruit01로 연결이 됨
    // 브라우저 주소창에 http://localhost:9000/fruit01를
    // 입력했을 때 이 메서드가 실행되도록 연결(매핑)함
    @GetMapping("/fruit01")
    public String test(Model model){
        // Model은 데이터 저장소 역할
        Fruit bean = new Fruit(); // model에 저장할 객체인 bean 생성

        // 객체 bean에 데이터 넣기
        bean.setId("banana");
        bean.setName("바나나");
        bean.setPrice(1000);

        // "fruit"이라는 이름으로 model에 bean 객체를 저장
        // "fruit"이라는 이름의 model은 return의 "fruit"이라는 이름의 html에서 사용됨
        model.addAttribute("fruit", bean);

        // (반환할 html파일 이름 적기 - 대소문자 구별함)
        // Spring MVC의 규칙에 따라 src/main/resources/templates/
        // 폴더 안에 있는 fruit.html 파일을 찾아 사용자 브라우저에 보여주라는 명령
        // (Thymeleaf 같은 템플릿 엔진에 의해서)
        return "fruit";
    }

    @GetMapping("/fruit01/list") // http://localhost:9000/fruit01/list
    public String test02(Model model){
        // 상품 여러개를 저장하기 위한 List 컬렉션
        // List 컬렉션 : 중복가능 / 순서따짐
        // 주로 게시물 목록들이나 상품 목록들을 나열할때 사용
        // add, remove, get, size 함수를 사용함

        // Generic (제네릭) : 클래스 내의 데이터 타입을
        // 클래스 밖에서 지정하여 타입을 일반화하는 것
        // 제네릭으로 설정한 타입으로 해당 배열, 컬렉션에 데이터가 들어감
        List<Fruit> fruitList = new ArrayList<>();

        fruitList.add(new Fruit("apple", "사과", 1000));
        fruitList.add(new Fruit("pear", "배", 2000));
        fruitList.add(new Fruit("grape", "포도", 3000));

        model.addAttribute("fruitList", fruitList);

        return "fruitList";
    }
}