package com.coffee.controller;

import com.coffee.entity.Product;
import com.coffee.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/product")
public class ProductController {
    @Autowired
    private ProductService productService ;

    // 상품 보기 페이지
    @GetMapping("/list")
    public List<Product> list(){
        // 내림차순으로 Product테이블의 데이터를 정렬한 것들을 products에 넣고 프론트에 보냄(response)
        List<Product> products = this.productService.getProductList() ;
        return products ;
    }

    // 상품 삭제
    // {id}를 경로 변수라고 부르며, 가변 매개 변수라고 부름
    // 상품 50번 누르면 id가 50으로 바뀌듯 그때그때 상황에 따라 바뀜
    // 경로변수를 delete() 안에 넣고 @PathVariable이라는 경로변수 어노테이션도 작성하면
    // 매핑에 있는 경로 변수 id가 delete의 id로 들어옴
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id){
        try {
            boolean isDeleted = this.productService.deleteProduct(id);

            if(isDeleted){ // isDeleted가 true면 삭제가 정상적으로 되었다는 뜻
                // 여기까지 왔다는 것은 에러없이 성공해서 내가 원하는 동작인 삭제가 된 것임
                return ResponseEntity.ok(id + "번 상품이 삭제되었습니다.");
            }else{ // 삭제를 할 상품 자체가 없는 상태
                return ResponseEntity.badRequest().body(id + "번 상품이 존재하지 않습니다.");
            }

        }catch (DataIntegrityViolationException err){// 데이터 베이스 무결성 위배되는 에러가 발생할때
            String message = "해당 상품은 장바구니에 포함이 되어 있거나, 이미 매출이 발생한 상품입니다. \n확인해 주세요." ;
            return ResponseEntity.internalServerError().body(message);

        }catch (Exception err){ // 두루뭉실한 예외처리
            return ResponseEntity.internalServerError().body("오류 발생 : " + err.getMessage());

        }
    }

    // 상품 등록
    @PostMapping("/insert")
    public ResponseEntity<?> insert(@Valid @RequestBody Product product, BindingResult bindingResult) {
        // bindingResult에 문제가 있으면
        // 다른 행동하지 않고 바로 에러 내용을 되돌림
        // 에러 내용은 엔티티에 적힌 오류 메시지 ("가격은 100원이상이어야 합니다." 같은것)임
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError xx : bindingResult.getFieldErrors()) {
                errors.put(xx.getField(), xx.getDefaultMessage());
            }

            return new ResponseEntity<>(
                    Map.of(
                            "message", "상품 등록 유효성 검사에 문제가 있습니다.",
                            "errors", errors
                    ),
                    HttpStatus.BAD_REQUEST
            );
        }

        try { // 성공하면
            // 이미지를 운영체제에 저장하고 상품의 이미지 컬럼에 값을 넣고 데이터베이스에 상품을 저장함
            Product savedProduct = this.productService.insertProduct(product);

            if (savedProduct == null) {
                return ResponseEntity
                        .status(500)
                        .body(
                                Map.of(
                                        "message", "상품 등록에 실패하였습니다.",
                                        "error", "bad image file format"
                                )
                        );
            }

            return ResponseEntity.ok(
                    Map.of(
                            "message", "상품이 성공적으로 등록되었습니다.",
                            "image", savedProduct.getImage()
                    )
            );

        } catch (IllegalStateException err) { // 경로 또는 이미지 저장 문제
            return ResponseEntity
                    .status(500)
                    .body(
                            Map.of(
                                    "message", err.getMessage(),
                                    "error", "File Save Error"
                            )
                    );

        } catch (Exception err) { // 데이터 베이스 오류
            return ResponseEntity
                    .status(500)
                    .body(
                            Map.of(
                                    "message", err.getMessage(),
                                    "error", "Internet Server Error"
                            )
                    );

        }
    }

    // 상품 수정 페이지 get 방식
    // 프론트 앤드의 상품 수정 페이지에서 요청이 들어 왔습니다.
    @GetMapping("/update/{id}") // 상품의 id 정보를 이용하여 해당 상품 Bean 객체를 반환해 줍니다.
    public ResponseEntity<Product> getUpdate(@PathVariable Long id){
        System.out.println("수정할 상품 번호 : " + id);

        // id에 해당하는 상품 정보 가져오기
        Product product = this.productService.getProductById(id) ;

        if(product == null){ // 상품이 없으면 404 응답과 함께 null을 반환
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

        }else{ // 해당 상품의 정보와 함께, 성공(200) 메시지를 반환합니다.
            return ResponseEntity.ok(product);
        }
    }

    // 상품 수정 페이지 put 방식
    @PutMapping("/update/{id}")
    public ResponseEntity<?> putUpdate(@PathVariable Long id,
                                       @Valid @RequestBody Product updatedProduct,
                                       BindingResult bindingResult) {
        // 1. 유효성 검사
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            return new ResponseEntity<>(
                    Map.of(
                            "message", "상품 수정 유효성 검사에 문제가 있습니다.",
                            "errors", errors
                    ),
                    HttpStatus.BAD_REQUEST
            );
        }

        // 2. 상품 조회
        Optional<Product> findProduct = productService.findById(id);

        if (findProduct.isEmpty()) {
            // ResponseEntity.notFound().build(): 지울 대상이 없으므로 깔끔하게
            // HTTP 상태 코드 404 Not Found 봉투만 빌드(build())해서 리액트에게 던짐
            return ResponseEntity.notFound().build();
        }

        try { // findProduct의 타입이 Optional이여서 굳이 get()을 하고 Product타입의 변수에 다시 넣음
            Product savedProduct = findProduct.get();
            // 새로운 Product의 정보를 기존에 저장된 Product정보에 덮어쓰기함
            productService.updateProduct(savedProduct, updatedProduct);

            // 프론트에는 성공했다는 메시지를 보냄
            return ResponseEntity.ok(Map.of("message", "상품 수정 성공"));

        } catch (Exception err) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", err.getMessage(),
                            "error", "상품 수정 실패"
                    ));
        }
    }

    // 상품 상세 페이지 get매핑 요청에 대한 응답을 위한 코드
    @GetMapping("/detail/{id}")
    public ResponseEntity<Product> detail(@PathVariable Long id){
        // Optional 타입인 product를 다시 Product 타입으로 선언
        Product product = productService.getProductById(id) ;

        if (product == null){ // product가 존재하지 않으면 오류코드를 만들어서 프론트에 응답함
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }else{ // 정상적인 데이터를 가진 product라면 프론트에 오류 없다는 상태 코드와 함께 응답함
            return ResponseEntity.ok(product);
        }
    }

}