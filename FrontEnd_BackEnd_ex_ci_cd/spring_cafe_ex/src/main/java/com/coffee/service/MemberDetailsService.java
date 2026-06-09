package com.coffee.service;

import com.coffee.entity.Member;
import com.coffee.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service // 클래스는 서비스 목적으로 사용된다.
@RequiredArgsConstructor
// 스프링 시큐리티에게 "로그인할 때 DB에서 유저 정보 찾는 로직은 내가 커스텀하게 만든 이 클래스를 사용해 줘!"라고 선언
public class MemberDetailsService implements UserDetailsService {
    private final MemberRepository memberRepository ;

    @Override // 사용자가 로그인할때 스프링 시큐리티가 인증 처리를 시작하면서 이 메소드를 자동으로 호출합니다.
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email) ;

        if(member == null){
            String message = "이메일이 " + email + "인 회원은 존재하지 않습니다.";

            // 존재하지 않는 회원이니까 예외를 발생시켜야함(일으켜야함)
            // 자바에서 사용자가 예외를 발생시키고자 하는 경우에는 throw 키워드를 사용합니다.
            throw new UsernameNotFoundException(message); // 객체임

        }else{
            // 나중에 디버깅할때 어디서 나왔는지 알려고 메소드명을 적음
            System.out.println("loadUserByUsername() 메소드");
            System.out.println(member);
        }
        // User는 UserDetails(인터페이스-구현체필요)와 상속관계여서 구현체로 사용함
        // DB에서 꺼내온 진짜 알맹이 데이터들(Member)을 시큐리티 표준 박스(User)의 칸막이에 맞춰서 이사시키는 작업
        // DB에 저장된 정보를 가진 User 객체를 시큐리티 엔진에게 보내면 시큐리티가 알아서
        // DB에 있는 암호화된 비밀번호와 사용자가 방금 로그인 창에 입력한 비밀번호와 비교해서
        // 최종 로그인을 통과시키거나 거부하게 됨
        return User.builder()
                .username(member.getEmail())
                .password(member.getPassword())
                .roles(member.getRole().name())
                .build();
    }
}