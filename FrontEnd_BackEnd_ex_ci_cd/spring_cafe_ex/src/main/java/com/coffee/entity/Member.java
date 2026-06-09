package com.coffee.entity;

import com.coffee.constant.Role;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@Entity // JPA에게 이 클래스는 DB랑 연결해서 관리해야 할 클래스라는 것을 알리는 어노테이션
@Table(name = "members") // 테이블로 설정하고 테이블 이름을 설정함
public class Member {
    @Id // 프라이머리 키로 설정하기
    // 값 생성하는 어노테이션
    // 방법으로 자동 생성하는 값을 넣음
    @GeneratedValue(strategy = GenerationType.AUTO)
    // 컬럼명 따로 설정하기 - 자바의 변수명은 id지만 DB컬럼명은 member_id가 됨
    @Column(name = "member_id")// pk컬럼명 : 테이블단수명_id
    private Long id ;
    // id를 int가 아니라 Long으로 타입을 설정한 이유
    // int는 4바이트 사용 / Long은 8바이트 사용 -> Long이 더 많은 숫자 사용가능
    // Long은 int보다 많은 숫자를 설정할 수 있어서 보험용으로 Long을 사용하는게 관례임

    @NotBlank(message = "이름은 필수 입력 사항입니다.")
    // 빈칸으로 두면 안되게 설정하고 텍스트를 표시함(데이터베이스의 제약조건느낌)
    private String name ;

    // 컬럼에 제약조건을 속성으로 설정할 수 있음
    @Column(unique = true, nullable = false)
    @NotBlank(message = "이메일은 필수 입력 사항입니다.")
    // 빈칸으로 두면 안되게 설정하고 텍스트를 표시함(데이터베이스의 제약조건느낌)
    @Email(message = "올바른 이메일 형식으로 입력해 주셔야 합니다.")
    // 이메일 형식인지 아닌지 검사하는 것 / 틀리면 메시지 출력
    private String email ;

    @NotBlank(message = "비밀 번호는 필수 입력 사항입니다.")
    // 빈칸으로 두면 안되게 설정하고 텍스트를 표시함(데이터베이스의 제약조건느낌)
    @Size(min = 8, max = 255, message = "비밀 번호는 8자리 이상, 255자리 이하로 입력해 주세요.")
    // 비밀번호의 사이즈 입력 조건 / 틀리면 메시지 출력
    @Pattern(regexp = ".*[A-Z].*", message = "비밀 번호는 대문자 1개 이상을 포함해야 합니다.")
    @Pattern(regexp = ".*[!@#$%].*", message = "비밀 번호는 특수 문자 '!@#$%' 중 하나 이상을 포함해야 합니다.")
    private String password ;

    @NotBlank(message = "주소는 필수 입력 사항입니다.")
    // 빈칸으로 두면 안되게 설정하고 텍스트를 표시함(데이터베이스의 제약조건느낌)
    private String address ;

    // Enum의 상수를 문자열 형태로 DB에 저장하겠다는 어노테이션
    @Enumerated(EnumType.STRING)
    private Role role ;

    // LocalDate를 직렬화할때 사용하는 규칙(패턴) 설정
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate regdate ; // 등록 일자
}