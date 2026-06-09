package com.coffee.service;

import com.coffee.entity.Product;
import com.coffee.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository ;

    // ProductRepository에서 만든 쿼리 메소드를 이용해서 메소드만들어서 Controller에 이용할 예정
    public List<Product> getProductList(){
        return this.productRepository.findProductByOrderByIdDesc();
    }

    // application.properties에서 이미지가 있는 실제 위치 가져오기
    @Value("${productImageLocation}")
    private String productImageLocation ;

    // 상품 id를 이용한 삭제
    // 1) 삭제할 상품이 데이터 베이스에 실제로 존재하는지 id를 통해 확인
    // 2) 운영체제에서 상품 삭제
    // 3) 상품 자체를 삭제

    // 1) 상품이 존재 하는지 확인
    public boolean deleteProduct(Long id){
        // 상품 가져오기 / 상품 id에 맞는 상품이 없으면 오류가 나니까
        // orElse()을 넣어서 ()안에 있는 내용을 가져오기 (null)을 가져옴
        Product product = productRepository.findById(id).orElse(null) ;

        // 상품id에 맞는 상품이 없으면
        if(product == null){
            return false ; // 없는 상품을 삭제할 수 없으니 상품 삭제 메소드는 실패해서 (false)를 반환함
        }

        // 상품id에 맞는 상품이 있으면
        // 2) 이미지가 있는 c드라이브 폴더(운영체제 내)에 가서 이미지 지우기
        String fileName = product.getImage() ;
        if(fileName != null && !fileName.isEmpty()){ // 다시 한 번 정상적인 파일이름인지 확인하기
            // application.properties에서 가져온 이미지 위치 연결하기
            File file = new File(productImageLocation + fileName) ;

            System.out.println("삭제될 파일 이름");
            // ex) C:\\upload\images\americano.jpg
            System.out.println(file.getAbsolutePath()); // 절대 경로 보여주기

            if(file.exists()){ // 해당 경로에! 삭제를 원하는 그 파일이 존재하는지 확인
                // 실제로 해당 경로의 파일을 삭제하고
                // 성공하면 true / 실패하면 false 반환함
                boolean deleted = file.delete() ;

                if(!deleted){ // 조건이 성립되려면 deleted가 false여야 !deleted가 true여서 메소드가 시작됨
                    System.out.println("이미지 삭제 실패");
                }
            }
        }
        // 3) 데이터 베이스에서 상품 삭제하기
        // 운영체제에서 이미지 파일을 삭제했으니 데이터 베이스에서도 삭제하기
        productRepository.deleteById(id);
        return true; // deleteProduct() 메소드 반환
    }

    /* 상품 등록 기능 */
    // 운영체제에 이미지 저장
    // 리액트가 보내주는 이미지의 Base64 인코딩 문자열을 변환하여 이미지로 만들고, 저장해주는 메소드입니다.
    // 운영체제에 이미지 저장하고 파일명 리턴하는 메소드
    private String saveProductImage(String base64Image) {
        // 데이터 베이스와 이미지 경로에 저장될 이미지의 이름
        // 현재 시각을 '년월일시분' 포맷으로 변환 (예: 202510171430)
        // formatter는 양식 데이터를 가지고 있음
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        // formatter 양식에 맞춘 현재 시각 데이터를 formattedNow에 저장함
        String formattedNow = LocalDateTime.now().format(formatter);

        // 데이터 베이스와 이미지 경로에 저장될 이미지의 이름
        String imageFileName = "product_" + formattedNow + ".jpg";

        // String 클래스 공부 : endsWith(), split() 메소드
        // String 클래스의 split() 메소드는 2개를 쪼깨는 것인데 무조건 리턴값(결과값)은 배열로 생성이 됨
        // split() 소괄호 안에 분리하는 기준점을 적고 뒤에 [] 대괄호 안에 0은 전자 1은 후자라고 생각하고 적으면 됨

        // 아직 운영체제에 실제 이미지 파일은 없음
        File imageFile = new File(productImageLocation + imageFileName);
        System.out.println("이미지 이름");
        System.out.println(imageFile.getName());

        // base64Image : JavaScript FileReader API에 만들어진 이미지의 문자열 데이터
        // 메소드 체이닝 : 점을 연속적으로 찍어서 메소드를 계속 호출하는 것
        // base64Image.split(",")[1] : 리액트에서 받아온 문자열 데이터의 값에서
        // ","을 기준으로 반으로 쪼개고 요소가 2개인 배열을 만들고 0(앞)과 1(뒤)중에 1을 선택함
        // (뒤가 우리가 필요한 이미지 데이터 텍스트임)
        // ex) data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUA...
        // {data:image/png;base64, iVBORw0KGgoAAAANSUhEUgAAAAUA...}
        // -> base64Image.split(",")[1] : iVBORw0KGgoAAAANSUhEUgAAAAUA...
        // 이렇게 가져온 이미지 데이터 텍스트를 다시 이미지 바이너리 바이트 데이터로 바꾸고
        // byte타입의 decodedImage 배열에 넣음
        byte[] decodedImage = Base64.getDecoder().decode(base64Image.split(",")[1]);

        // FileOutputStream는 바이트 파일을 처리해주는 자바의 Stream 클래스
        // new FileOutputStream(imageFile) : imageFile 경로에 빈 파일을 하나 생성 / 바이트 데이터 넣을 준비
        // 빈 파일의 객체에 이미지 바이너리 바이트 데이터를 넣어서 실제 이미지 파일로 생성
        // 파일 정보를 byte 단위로 변환하여 이미지를 복사합니다.
        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            fos.write(decodedImage);
            // 성공했으니 데이터 베이스에 기록할 파일명을 리턴함
            // -> insertProduct() 메소드에서 imageFileName 변수 사용이 가능해짐
            return imageFileName;
        } catch (Exception e) {
            throw new IllegalStateException("이미지 파일 저장 중 오류가 발생했습니다.");
        }
    }

    // 데이터 베이스에 새로운 상품 등록하기
    public Product insertProduct(Product product) {
        if (product.getImage() != null && product.getImage().startsWith("data:image")) {
            // saveProductImage() 메소드를 실행해서 운영체제에 이미지를 넣고
            // return받은 이미지 파일이름을 상품의 Image 컬럼에 넣기
            String imageFileName = saveProductImage(product.getImage());
            product.setImage(imageFileName);
        }

        product.setInputdate(LocalDate.now());
        System.out.println("서비스)상품 등록 정보");
        System.out.println(product);

        // save() 메소드는 CrudRepository에 포함되어 있습니다.
        return productRepository.save(product);
    }


    /* 상품 수정 기능 */
    // 상품 수정하기 get 방식 시작 (id를 이용해서 특정 상품 정보 가져오기)
    public Product getProductById(Long id) {
        // Optional : 해당 상품이 있을 수도 있지만, 경우에 따라서 없을 수도 있습니다.
        // findById()는 무조건 결과를 Optional 타입으로 돌려줌
        // id에 해당하는 데이터를 가져와서 product에 넣어주는데 오류를 방지하기 위해 Optional 타입으로 줌
        Optional<Product> product = this.productRepository.findById(id);

        // 의미 있는 데이터이면 그냥 넘기고, 그렇지 않으면 null을 반환해 줍니다.
        return product.orElse(null);
    }


    // 상품 수정하기 put 방식 시작
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    // 이전 이미지 파일을 삭제하는 메소드
    private void deleteOldImage(String oldImageFileName) {
        if (oldImageFileName == null || oldImageFileName.isBlank()) {
            return;
        }

        File oldImageFile = new File(productImageLocation + oldImageFileName);

        if (oldImageFile.exists()) { // 예전파일이 운영체제에 존재하면 삭제함
            boolean deleted = oldImageFile.delete();
            if (!deleted) {
                System.err.println("기존 이미지 삭제 실패 : " + oldImageFileName);
            }
        }
    }

    // Product 수정
    // 저장된 상품 정보를 들고와서 새로 수정된 상품 정보로 대체함
    public Product updateProduct(Product savedProduct, Product updatedProduct) {
        savedProduct.setName(updatedProduct.getName());
        savedProduct.setPrice(updatedProduct.getPrice());
        savedProduct.setCategory(updatedProduct.getCategory());
        savedProduct.setStock(updatedProduct.getStock());
        savedProduct.setDescription(updatedProduct.getDescription());

        if (updatedProduct.getImage() != null && updatedProduct.getImage().startsWith("data:image")) {
            // 저장된 상품 정보의 이미지 파일 삭제
            deleteOldImage(savedProduct.getImage());
            // saveProductImage() : 운영체제에 이미지 저장하고 파일명 리턴하는 메소드를 재사용함
            String imageFileName = saveProductImage(updatedProduct.getImage());
            savedProduct.setImage(imageFileName);
        }

        // 새로운 정보를 사진 상품을 데이터 베이스에 다시 저장함
        return productRepository.save(savedProduct);
    }

    // 프론트의 ProductDetail.tsx의 addToCart() 함수에 의해? 필요함
    // Optional은 값이 있을 수도 없을 수도 있음을 표현한 타입
    // Optional 타입으로 되어있는 데이터는 사용할때 반드시 꺼내야함
    // Product product = findProductById(id).orElse(null); 이런식으로 꺼내던지
    // Product product = findProductById(id).orElseThrow(() -> new RuntimeException("회원 없음")); 이런식으로 함
    // Product.id로 데이터 베이스에서 Product의 데이터를 찾는 함수
    public Optional<Product> findProductById(Long productId) {
        return productRepository.findById(productId);
    }

}