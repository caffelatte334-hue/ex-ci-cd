package com.coffee.repository;

import com.coffee.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    // 쿼리 메소드 findProductByOrderByIdDesc()
    // 상품(Product)들을 가져오는데, ID 번호가 큰 것부터(최신순으로) 정렬해서 다 가져와라!
    // Service가 이용하고 그걸 또 Controller가 이용함
    // find테이블By : 조회해라 (select)
    // Product : Product 테이블
    // OrderBy컬럼 : 컬럼을 기준으로 정렬해라
    // Id : Id 컬럼
    // Desc : 내림차순 (큰수부터 아래로) (최신순 정렬)
    List<Product> findProductByOrderByIdDesc();
}