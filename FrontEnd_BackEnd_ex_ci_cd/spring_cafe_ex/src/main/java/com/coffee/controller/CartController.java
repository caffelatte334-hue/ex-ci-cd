package com.coffee.controller;

import com.coffee.dto.CartItemDto;
import com.coffee.dto.CartProductDto;
import com.coffee.entity.Member;
import com.coffee.service.CartProductService;
import com.coffee.service.CartService;
import com.coffee.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {
    private final CartService cartService ;

    @PostMapping("/insert") // /cart/insert
    public ResponseEntity<String> addToCart(@RequestBody CartProductDto dto,
                                            Authentication authentication){
        // dto에는 productId와 quantity가 들어 있음 (memberId는 있지만 null값임)

        // JwtAuthenticationFilter.java 파일에서 만든
        // UsernamePasswordAuthenticationToken 타입의 객체인 auth를 만들때 넣은
        // 매개변수 email이 (이름만 email이지 사실상 들어온 token의 claim에서 subject(식별자)의 값을 넣은 변수임)
        // principal 위치에 있고
        // SecurityContextHolder.getContext().setAuthentication(auth); 으로
        // authentication은 그 값을 가지고 있는데 name을 principal의 위치에 있는 걸로 인식해서
        // getName() 함수를 실행하면 인증 객체의 principal(토큰에서 가져온 email 값)을 가져옴
        String email = authentication.getName() ;
        String message = cartService.addProductToCart(dto, email);

        return ResponseEntity.ok(message) ;
    }

    // 토큰으로 만든 인증 객체에 담긴 토큰의 subject인 email을 이용해서 member 객체를 DB에서 가져오기 위해 준비
    private final MemberService memberService ;

    @GetMapping("/list")
    public ResponseEntity<List<CartItemDto>> getCartProducts(Authentication authentication){
        // 토큰으로 만든 인증 객체에 담긴 토큰의 subject인 email을 이용해서
        // 해당 email의 member 객체를 DB에서 가져옴
        String email = authentication.getName();

        Member member = memberService.findByEmail(email) ;

        if (member == null){
            new RuntimeException("사용자가 존재하지 않습니다.");
        }

        // 해당 맴버의 Id를 이용해 DB에서 해당 맴버의 cart에 들어있는 상품 객체들을
        // 프론트에서 받을 준비가 된 객체 타입인 CartItemDto 타입으로 담아서 반환함
        return ResponseEntity.ok(cartService.getCartItemsByMemberId(member.getId())) ;
    }

    private final CartProductService cartProductService ;

    @PatchMapping("/edit/{cartProductId}")
    public ResponseEntity<String> editCartProductQuantity(
            @PathVariable Long cartProductId, // url 경로 (필수) (Mapping 어노테이션에 url주소로 들어가야함)
            // 쿼리 파라미터 부가옵션 (선택) (url주소로 안들어가도 됨)
            @RequestParam(required = false) Integer quantity,
            @RequestParam(required = false) Long productId){
        System.out.println("카트 상품 아이디 : " + cartProductId);
        System.out.println("변경할 갯수 : " + quantity);
        System.out.println("상품 아이디 : " + productId);

        String message = cartProductService.editCartProductQuantity(cartProductId, quantity, productId) ;

        if (message.startsWith("오류")){// CartProductService에 해당 함수에 if문으로 오류가 return되는 경우 이용
            return ResponseEntity.badRequest().body(message) ;
        }else{
            return ResponseEntity.ok(message) ;
        }
    }

    @DeleteMapping("/delete/{cartProductId}")
    public ResponseEntity<String> deleteCartProduct(@PathVariable Long cartProductId){
        System.out.println("삭제할 카트 상품 아이디 : " + cartProductId);

        cartProductService.deleteCartProductById(cartProductId);

        String message = "카트 상품 " + cartProductId + "번이 장바구니 목록에서 삭제되었습니다.";
        return ResponseEntity.ok(message) ;
    }
}