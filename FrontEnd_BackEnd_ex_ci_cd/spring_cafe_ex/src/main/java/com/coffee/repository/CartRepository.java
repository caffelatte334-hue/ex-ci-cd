package com.coffee.repository;

import com.coffee.entity.Cart;
import com.coffee.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    // 이 member가 가진 카트 정보가 있는지 조회하는 메소드 (카트를 가지고 있는지 없는지)
    // SELECT * FROM carts WHERE member_id = ?;
    // carts 테이블의 member_id 컬럼이 ? 인 데이터를 다 조회해라
    // Java에서는 Member member 객체로 찾지만
    // Cart 엔티티를 보면 @JoinColumn(name = "member_id")으로 되어 있어서
    // JPA에 의해 매개변수 Member타입인 member를 자동으로 member_id로 변환해서 넣어주는 것처럼
    // DB에는 적용이 됨
    Optional<Cart> findByMember(Member member);
}
