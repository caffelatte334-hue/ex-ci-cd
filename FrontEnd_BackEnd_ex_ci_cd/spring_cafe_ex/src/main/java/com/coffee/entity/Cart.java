package com.coffee.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter @Setter @ToString
@Entity
@Table(name = "carts")
public class Cart {
    @Id // 기본키(pk) 설정
    @GeneratedValue(strategy = GenerationType.AUTO) // 숫자 자동 생성
    @Column(name = "cart_id") // 실제 데이터 베이스 컬럼명은 cart_id
    private Long id ;

    // 연관 관계 매핑 (Entity간의 연관 관계 서술)
    // fetch : 데이터 베이스에서 데이터를 가져온다는 뜻
    // (우리는 axios를 사용하지만 자바의 기본 기능이 fetch)
    // FetchType.LAZY : 즉시로딩 말고 지연로딩을 의미함 (진짜 필요할때 - member에 접근할때 가져오기)
    // 부모자식 참조관계에서 일대일, 일대다, 다대일은 중요하지 않음
    // 어떤 관계든 자식이 부모의 PK를 FK로 가진다
    // 해당클래스(엔티티)to어노테이션이_붙은_컬럼 : to의 앞에가 해당 클래스고 to의 뒤에가 매핑된 엔티티(테이블)
    @OneToOne(fetch = FetchType.LAZY)
    // @JoinColumn은 자식 테이블에 적어야 하는 어노테이션임 (조인 어노테이션) (외래키 정의함)
    @JoinColumn(name = "member_id") // fk이름은 members테이블의 pk 컬럼명과 동일하게 적어야 함 (관례임)
    private Member member ; // carts테이블에 member_id라는 이름의 members테이블의 fk인 컬럼이 생성됨

    // 여기에는 @JoinColumn을 적지 않음. 왜? (부모 테이블이여서)
    // 카트안에는 카트상품이 여러개 담길 수 있어서 컬렉션(중에서도 List)로 변수 생성해야 함
    // 카트에는 여러 개의 '카트 상품'들이 담겨야 하므로 List가 좋습니다
    // mappedBy에는 자식 테이블의 적힌 부모 테이블인 클래스의 이름으로 된 맴버변수를 적음
    // (자식 테이블(엔티티)인 CartProduct에 있는 fk로 설정된 cart 맴버변수)

    // * 해당 변수의 값은 JPA가 Cart에 연결된 CartProduct들을 cart_products 테이블에서 찾아서
    // * List<CartProduct> cartProducts의 값에 채워 넣어줌
    // -> 한마디로 cart_products 테이블에서 cart_id 컬럼의 값이 이 Cart타입의 객체의 id와 같으면
    // 그 데이터들을 가지고 와서 이 Java 변수인 List<CartProduct> cartProducts에 넣음
    // FetchType.LAZY여서 실제로 Cart.getCartProducts()로 호출을 하는 순간 조회해서 넣어줌
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CartProduct> cartProducts ;
}