package com.coffee.controller;

import com.coffee.config.JwtTokenProvider;
import com.coffee.dto.LoginDto;
import com.coffee.entity.Member;
import com.coffee.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    private final AuthenticationManager authenticationManager ;
    private final JwtTokenProvider jwtTokenProvider ;

    @PostMapping("/login")
    // LoginDto의 데이터를 객체에 담으려고 @RequestBody 작성
    // Long타입과 String타입을 동시에 만족하는 타입은 최상위 타입인 Object타입이여서 Object타입도 적음
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginDto dto){
        // 인증 처리
        // username, password, roles 정보를 가진 객체를 이용
        // 스프링 시큐리티가 자동으로 웹페이지에서 받아온 정보과 비교
        // username - 받아온 정보와 내가 가진 정보가 맞는지
        // password - 올바른 비밀번호를 썼는지 (이게 핵심)
        // roles - 비밀번호 확인이 되면 지정해줄 roles
        // authenticationManager.authenticate : UsernamePasswordAuthenticationToken에
        // 임시로 담은 정보를 MemberDetailsService의 User의 정보와 비교해서 비밀번호가 대응되면
        // 인증서 (authentication)를 발급해줌
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(// 프론트에서 가져온 로그인 정보를 담은 객체를 임시로 담음
                        dto.getEmail(),
                        dto.getPassword()
                )
        );

        // 사용자 정보 조회
        // 비밀번호 대조가 끝났으니 리액트에게 응답할 유저의 진짜 데이터 정보를 담은 회원 객체를 가져옴
        Member member = memberService.findByEmail(dto.getEmail());

        if(member == null){ // 정상적이면 null이 될 수 없지만 혹시 모르니까 검사함
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "사용자 정보를 찾을 수 없습니다."));
        }else{
            // JWT 토큰 생성하기
            String token = jwtTokenProvider.createToken(member);

            // 응답 (사용자 정보 조회로 가져온 회원의 진짜 데이터 정보들을 응답함)
            return ResponseEntity.ok(Map.of("accessToken", token, "id", member.getId(),
                    "name", member.getName(), "email", member.getEmail(),
                    "role", member.getRole().toString())) ;
        }


    }

    // @Valid : Member 엔티티에 @NotBlank(공백 불가)나 @Size(글자수 제한) 같은 유효성 검사 규칙이 있다면
    // 프론트엔드에서 데이터를 보낼때 해당 규칙에 맞게 보냈는지 검사하는 어노테이션
    // BindingResult : 방금 @Valid로 검사했을때 오류가 있는지 없는지에 대한 결과를 담는 바구니
    // @RequestBody : 리액트가 보낸 JSON 데이터를 Java 형식으로 변환해서 Member 객체(변수명 bean)에 넣음
    // 따라서 리액트가 보내는 파라미터의 이름과 그걸 받는 엔티티(Member)의 맴버 변수의 이름이 동일해야 자동 매핑됨
    // Controller까지는 그대로의 데이터, 그 이후인 Service에서 암호화를 하든 말든 함
    @PostMapping("/signup")
    // ResponseEntity<?> : http 상태 코드, 헤더(부가정보), 바디(프론트가 쓸 알맹이)
    // 이 3가지를 한번에 보낼 수 있는 반환타입 (<?> 이 와일드 카드는 여러 타입의 데이터가 동시에 들어가도 상관없다는 뜻)
    // 만약에 에러코드 (ex-400)를 반환하면 리액트에서는 try로 안들어가고 바로 catch (error)로 들어감
    public ResponseEntity<?>signup(@Valid @RequestBody Member bean, BindingResult bindingResult){
        System.out.println("회원 가입 정보");
        System.out.println(bean);

        // hasErrors() : 에러가 존재하면 true를 리턴함 (SpringBoot 교안 PDF (P.146))
        if(bindingResult.hasErrors()){
            Map<String, String> errors = new HashMap<>();
            // 확장 for 사용
            // bindingResult의 getFieldErrors() 바구니에서 에러들을 FieldError 타입으로 꺼내서 Map에 넣음
            for(FieldError xx:bindingResult.getFieldErrors()){// Field:Java에서의 변수(name,password등)
                errors.put(xx.getField(), xx.getDefaultMessage());
            }
            System.out.println(errors);
            // Map 형식을 프론트에 반환함
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }else{
            System.out.println("ok");
        }

        // 이메일 중복 체크
        Member member = memberService.findByEmail(bean.getEmail());
        if (member != null){ // member가 null이 아니라는 것은 이미 존재하는 id(맴버)라는 것을 의미함
            // 이미 존재하는 이메일 주소
            // Map.of : Map 컬렉션의 축약버전
            // <>안에 원래 타입을 적어줘야하는데 스프링이 알아서 Map<String, String>을 뒤에 Map.of보고 추론함
            // ResponseEntity(T body, HttpStatus status) 생성자 이용
            return new ResponseEntity<>(Map.of("email", "이미 존재하는 이메일 주소입니다."),
                    HttpStatus.BAD_REQUEST);
        }

        // 회원 가입 처리
        memberService.insert(bean); // memberService에서 insert메소드를 넣어서 나머지 정보들도 넣음
        return new ResponseEntity<>("회원 가입 성공", HttpStatus.OK) ; // 회원 가입 성공 (OK라는건 200번대라는 뜻)
    }
}
