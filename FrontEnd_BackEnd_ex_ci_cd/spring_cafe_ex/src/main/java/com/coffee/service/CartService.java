package com.coffee.service;

import com.coffee.dto.CartItemDto;
import com.coffee.dto.CartProductDto;
import com.coffee.entity.Cart;
import com.coffee.entity.CartProduct;
import com.coffee.entity.Member;
import com.coffee.entity.Product;
import com.coffee.repository.CartRepository;
import com.coffee.repository.MemberRepository;
import com.coffee.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService { // 필요한 변수들 생성
    private final CartRepository cartRepository ;
    private final MemberService memberService ;
    private final ProductService productService ;
    private final CartProductService cartProductService ;
    private final MemberRepository memberRepository ;
    private final ProductRepository productRepository ;

    // carts 테이블에 cart 객체 저장하는 함수 (+ 저장한 객체를 Cart 타입으로 반환도 함)
    public Cart saveCart(Cart cart){
        return cartRepository.save(cart);
    }

    // 매개변수 cart에 들어있는 카트상품이 매개변수 product와 같은 종류의 상품(product)인지 확인하는 함수
    // 맞으면 그 카트상품을 반환 / 아니면 null 반환
    private CartProduct findExistingProduct(Cart cart, Product product){
        // 해당 상품이 카트 내에 들어 있으면, 해당 상품 객체를 반환해주는 메소드
        // 동일한 상품이 이미 카트 내에 들어 있으면 수량을 누적할 목적임
        // cart.getCartProducts() : cart객체의 id를 cart_id이름의 fk인 컬럼의 데이터로 가진
        // CartProduct들을 가져와서 List 컬렉션에 넣고 반환함
        // -> 카트에 들어있는 카트 상품들을 객체로 컬렉션에 담아서 가져오는 것

        // 카트에 카드 상품이 없으면 null을 반환함
        if (cart.getCartProducts() == null) return null ;

        // 컬렉션안에 있는 객체들을 꺼내기 위해 사용하는 확장 for
        for(CartProduct cp : cart.getCartProducts()){
            // 카트에 담긴 카트 상품의 상품(상품종류)의 id가 매개변수인 product의 Id와 같으면
            // 그 카드 상품을 반환함
            if (cp.getProduct().getId().equals(product.getId())){
                return cp;
            }
        }
        return null ;
    }

    // @Transactional : 이 안에서 일어나는 모든 DB작업을 하나의 묶음(트랜잭션)으로 처리하라는 어노테이션
    // 중간에 에러, 예외가 터지면 그 전까지 했던 DB 변경을 전부 취소(롤백)하라는 의미
    // 이렇게 설정하는 이유는 중간 과정을 통틀어서 결국 최종 결과까지 가야하는데 중간에
    // 애매하게 바뀌고 결국 최종 결과까지 가지 못한상태로 DB가 변경이 되면 의미가 없고 꼬일 수 있기 때문에
    @Transactional
    public String addProductToCart(CartProductDto dto, String email){ // token의 claim에 있는 email을 사용함
        // dto : memberId(null) / productId, quantity는 프론트에서 값을 파라미터로 받음
        // 회원 조회 : email(아이디)을 이용해서 member를 찾음
        Member member = memberRepository.findByEmail(email) ;

        if (member == null) { // 회원이 없음
            throw new RuntimeException("회원 없음");
        }

        // 상품 조회 : 프론트에서 파라미터에서 가져온 productId 사용
        Product product = productRepository.findById(dto.getProductId())
                // 예외처리를 의도적으로 발생시키기 (Optional 타입이 반환되어서 .orElseThrow() 사용)
                .orElseThrow(() -> new RuntimeException("상품 없음")) ;


        // 재고 확인(주문 수량이 재고보다 많으면)
        // product.getStock() 상품의 재고 / dto.getQuantity() 프론트에서 받아온 유저가 선택한 주문수량
        if (product.getStock() < dto.getQuantity()){
            throw new RuntimeException("재고 수량이 부족합니다.");
        }

        // 장바구니 조회 또는 생성
        // 해당 맴버의 카트를 조회함 (cart는 member의 자식 테이블)
        // 해당 맴버의 카트를 객체에 저장함
        Cart cart = cartRepository.findByMember(member).orElse(null) ;

        if (cart == null){ // 카트가 구비 안된 고객
            Cart newCart = new Cart() ; // 새 카트 준비
            newCart.setMember(member); // 고객에게 할당
            // saveCart(Cart cart) : cartRepository.save(cart);
            cart = saveCart(newCart); // 해당 카트가 저장됨
        }

        // 기존 상품이 존재하는 지 확인 후 수량 처리
        // 카트에 있는 카트상품과 해당 상품을 비교해서 존재하면 해당 카트상품을 반환함
        CartProduct existingCartProduct = findExistingProduct(cart, product) ;

        if (existingCartProduct != null){ // 장바구니에 해당 상품이 들어 있으면
            // 기존 수량에 장바구니에서 요청항 수량을 누적합니다.
            // existingCartProduct.getQuantity() : 이미 카트에 들어 있는 값
            // dto.getQuantity() : 새로 추가 하는 값
            existingCartProduct.setQuantity(existingCartProduct.getQuantity()
                    + dto.getQuantity());

            // 서비스의 저장 메소드를 요청하여 database에 저장합니다.
            // 서비스를 요청하는데 이것이 Repository에 가서 database에 저장함
            // CartProductRepository.save(existingCartProduct);로 바로 DB에 저장해도 되지만
            // Service끼리는 Service끼리 소통해서 계층 구조를 지키려고 하는 것
            cartProductService.saveCartProduct(existingCartProduct);

        }else{ // 장바구니에 품목이 없는 경우
            CartProduct cp = new CartProduct();

            // 물건을 어떤 카트에 담을지 설정
            // cart : 해당 맴버의 카트
            cp.setCart(cart);

            // 어떤 물건 종류를 담을지 설정
            // product : 파라미터로 가져온 productId로 가져온 조회한 product (상품)
            cp.setProduct(product);

            // 웹페이지에서 입력한 수량 입력하기
            // 파라미터에서 가져와서 dto에 넣은 quantity
            cp.setQuantity(dto.getQuantity());

            // Repository에 보내기
            cartProductService.saveCartProduct(cp);
        }

        return "요청하신 상품이 장바구니에 추가되었습니다." ;
    }

    // 해당 유저의 id를 이용해서 해당 유저의 카트에 들어 있는 카트 상품들을
    // CartItemDto타입의 객체들로 담아서 프론트에 반환함
    public List<CartItemDto> getCartItemsByMemberId(Long memberId){
        // 회원 조회
        Member member = memberService.findMemberById(memberId)
                .orElseThrow(() -> new RuntimeException("유효하지 않은 회원입니다.")) ;

        // 회원이 소유한 카트 정보 조회
        // 없으면 빈 카트 생성
        // Cart::new : () -> new Cart()를 줄인 것
        Cart cart = cartRepository.findByMember(member).orElseGet(Cart::new);

        // 가져온(혹은 생성한) 카트 객체에 들어있는 CartProduct 타입의 객체들을 빼서
        // 프론트엔드에 보낼 객체인 CartItemDto 객체로 만들어서 List 컬렉션에 객체들을 넣고 반환함
        List<CartItemDto> cartItemDtoList = new ArrayList<>();
        for (CartProduct cp : cart.getCartProducts()){
            cartItemDtoList.add(new CartItemDto(cp));
        }
        return cartItemDtoList ;

	/* return cart.getCartProducts().stream()
			.map(CartItemDto::new).toList() ; */
    }
}