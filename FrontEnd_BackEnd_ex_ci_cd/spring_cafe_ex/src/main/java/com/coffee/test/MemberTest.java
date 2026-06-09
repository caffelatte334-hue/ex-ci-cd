package com.coffee.test;

import com.coffee.constant.Role;
import com.coffee.entity.Member;
import com.coffee.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestConstructor;

import java.time.LocalDate;

// 백엔드 기능이 실제로 잘 작동하는지 테스트해보기 위해서 만드는 테스트 클래스

// 스프링 부트의 모든 설정(Bean, 리파지토리 등)을 다 불러와서 테스트 환경을 만듭 / 실제 서버를 띄운 것과 거의 같은 환경을 빌려옴
@SpringBootTest
@RequiredArgsConstructor // 필수/재료/생성자
// final(필수)이 붙은 변수는 값이 무조건 있어야하는데 값이 없는 변수가 있으면 (의존하는 변수)
// 그 변수를 찾아서 아래와 같은 코드를 보이지 않게 자동으로 작성해줌 (주입해주기)
// public MemberTest(MemberRepository memberRepository) {
//    this.memberRepository = memberRepository;
//}
// 매개변수 MemberRepository memberRepository는 스프링이 미리 만들어 놓은 객체여서 이 객체의 값을 대입하면 사실상
// 이 MemberTest 클래스이 변수인 memberRepository가 객체가 되는거임
// 따라서 해당 객체의 맴버 변수와 맴버 메소드를 사용 가능함
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL) // 테스트용 어노테이션
public class MemberTest {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Test // main메소드가 없어도 run할 수 있게 만들어주는 어노테이션 (일종의 가짜 main메소드)
    @DisplayName("회원 몇 명 추가하기") // 테스트 결과 창에 insertMember라는 코드 이름을 ""안의 문자열로 보여줌
    // id는 자동생성되게 @GeneratedValue(strategy = GenerationType.AUTO)를 써서 setter하지 않음
    void insertMember(){
        Member mem01 = new Member();
        mem01.setName("관리자");
        mem01.setEmail("admin@naver.com");
        mem01.setPassword(passwordEncoder.encode("admin@naver.com")); // 비밀번호 암호화
        mem01.setAddress("마포구 공덕공");
        mem01.setRole(Role.ADMIN);
        mem01.setRegdate(LocalDate.now());

        memberRepository.save(mem01); // 리파지토리에 해당 객체의 데이터를 저장하기
        System.out.println("----------------------------------------");

        Member mem02 = new Member();
        mem02.setName("유영석");
        mem02.setEmail("bluesky@naver.com");
        mem02.setPassword(passwordEncoder.encode("Bluesky@456"));
        mem02.setAddress("용산구 이태원동");
        mem02.setRole(Role.USER);
        mem02.setRegdate(LocalDate.now());

        memberRepository.save(mem02) ;
        System.out.println("----------------------");

        Member mem03 = new Member();
        mem03.setName("곰돌이");
        mem03.setEmail("gomdori@naver.com");
        mem03.setPassword(passwordEncoder.encode("Gomdori@789"));
        mem03.setAddress("동대문구 휘경동");
        mem03.setRole(Role.USER);
        mem03.setRegdate(LocalDate.now());

        memberRepository.save(mem03) ;
        System.out.println("----------------------");
    }

}
