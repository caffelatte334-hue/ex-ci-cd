package com.coffee.repository;

import com.coffee.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

// 리파지토리는 설계도이자 부품이여서
// 이 부품을 쓰는 비즈니스 로직인 Service 클래스를 이용해서 사용함

// 리파지토리를 클래스가 아닌 인터페이스로 만드는 이유 :
// 리자피토리 기능을 수행하기 위해서 JpaRepository 인터페이스를 상속받아야 하는데
// 클래스로 이 인터페이스를 상속받으면 이 인터페이스에 있는 모든 메소드를 반드시
// 오버라이드해야하는데 인터페이스들끼리 상속받으면 이 의무가 없기때문에 효율적으로 사용이 가능함

// JpaRepository : Spring에서 제공하는 부모 인터페이스 / 기본적인 CRUD(Create, Read, Update, Delete) 메소드를 이미 다 가지고 있음
// <Member, Long> : 제레닉 : 이 repository가 관리할 대상은 Member 클래스 / 관리 대상의 기본키로 설정된 변수의 데이터 타입
// 관리할 대상과 관리 대상의 기본키로 설정된 변수의 데이터 타입을 적는 이유는
// JPA는 이 2개로 DB에서 사용자가 원하는 정보를 찾기 때문에 그냥 DB를 사용한다면 기본키가 없어도 되지만
// JPA를 사용한다면 기본키가 무조건 있어야 한다.

// repository의 구체적인 역할 : 서비스(Service)로부터 요청을 받아서 실제 DB에 관여하는 역할
// CRUD(Create 생성-insert / Read 조회-select / Update 수정-update / Delete 삭제-delete)를 복잡한 SQL 명령어 대신 자바 언어를 써서
// DB를 조작할 수 있게 해주는 역할
// 이 자바 언어는 요청하는 곳인 서비스(Service)에 작성함
public interface MemberRepository extends JpaRepository<Member, Long> {
    // 이메일을 사용하여 회원 정보를 조회하는 쿼리 메소드
    // 인터페이스여서 만들어 놓기만 하고 Spring Data JPA가 자동으로 구체화 해서 실행해줌
    // findBy+컬럼명 = select(SQL) / Get 매핑 / read(CRUD)
    // 반환타입이 Member인 findByEmail이 메소드명인 메소드
    Member findByEmail (String email);

}
