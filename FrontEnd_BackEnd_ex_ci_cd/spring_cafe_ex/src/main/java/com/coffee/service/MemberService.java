package com.coffee.service;

import com.coffee.constant.Role;
import com.coffee.entity.Member;
import com.coffee.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService { // MemberService가 MemberRepository를 의존하고 있음
    // 의존 + 무의미한 데이터여서 주입(injection)해야 함 + final로 변경
    private final MemberRepository memberRepository;

    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    @Autowired // 필드 주입 : 맴버 변수에 직접 의존성을 주입하는 방식 (@RequiredArgsConstructor 이용해도 됨)
    private PasswordEncoder passwordEncoder;

    public void insert(Member bean) {
        // 회원 가입한 사용자의 역할과 등록 일자는 리액트에서 요청보낼때 파라미터로 주지 않고 여기서 설정
        bean.setRole(Role.USER);
        bean.setRegdate(LocalDate.now());

        String encodedPassword = passwordEncoder.encode(bean.getPassword());
        bean.setPassword(encodedPassword);

        memberRepository.save(bean);
    }

    // 프론트의 ProductDetail.tsx의 addToCart() 함수에 의해? 필요함
    // Optional은 값이 있을 수도 없을 수도 있음을 표현한 타입
    // Optional 타입으로 되어있는 데이터는 사용할때 반드시 꺼내야함
    // Member member = findMemberById(id).orElse(null); 이런식으로 꺼내던지
    // Member member = findMemberById(id).orElseThrow(() -> new RuntimeException("회원 없음")); 이런식으로 함
    // Member.id로 데이터 베이스에서 Member의 데이터를 찾는 함수
    public Optional<Member> findMemberById(Long memberId) {
        return this.memberRepository.findById(memberId);
    }
}