import { Button, Col, Container, Form, Image, Row, Table } from "react-bootstrap";

import type { User } from "../types/User";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import customAxios from './../api/axiosInstance';
import type { CartProduct } from "../types/CartProduct";
import { API_BASE_URL } from "../config/config";

type AppProps = {
    user: User | null; // user는 User 객체 혹은 null일 수도 있습니다.
}

function App({ user }: AppProps) {
    const thStyle = { fontSize: '1.2rem' }; // 테이블 헤더 스타일

    // 보여 주고자하는 `카트 상품` 배열 정보
    const [cartProducts, setCartProducts] = useState<CartProduct[]>([]);

    useEffect(() => { // user가 있고 user.id가 존재하면 해당 user의 cartlist를 가져옴
        if (user && user?.id) {
            fetchCartProducts();
        }
    }, [user]); // user가 바뀌면 화면 갱신하고 다시 그 user의 cartlist를 가져옴

    const navigate = useNavigate();

    const fetchCartProducts = async () => {
        try {
            const url = `${API_BASE_URL}/cart/list`;
            const response = await customAxios.get(url);
            console.log('카트 상품 조회 결과');
            console.log(response.data);

            // 백엔드의 CartItemDto 타입(프론트의 CartProduct.ts 타입)으로 된 객체들이 담김
            setCartProducts(response.data || []);

        } catch (error) {
            console.log('오류 정보');
            console.log(error);
            alert(`'카트 상품' 정보가 존재하지 않아서 상품 목록 페이지로 이동합니다.`);
            navigate('/product/list');

        }
    };

    // 화면에 보여 주는 주문 총 금액을 위한 스테이트 (체크한 품목에 따라 값이 변해야 함)
    const [orderTotalPrice, setOrderTotalPrice] = useState(0);

    // 체크 박스의 상태가 Toggle될 때 마다, 전체 요금을 다시 재계산하는 함수
    // (총 주문 금액 변화)
    // products는 카트에 담긴 상품들을 의미 (백엔드에서 받아온 데이터)
    // CartProduct[]는 타입스크립트(설계도) - 스프링의 CartItemDto도 동일한지 확인해야 함
    const refreshOrderTotalPrice = (products: CartProduct[]) => {
        let total = 0; // 총 금액 변수 // 처음에는 초기화 해놓음

        // bean은 상품 하나를 의미
        products.forEach((bean) => {
            if (bean.checked) { // 선택된 체크 박스에 대하여
                total += bean.price * bean.quantity; // 총 금액 누적
            }
        });

        setOrderTotalPrice(total); // State 업데이트
    };

    // `전체 선택` 체크 박스를 Toggle 함
    const toggleAllCheckBox = (isAllCheck: boolean) => {
        // isAllCheck : `전체 선택` 체크 박스의 boolean 값
        setCartProducts((previous) => {
            // 모든 객체(카트 상품)들의 나머지 속성은 보존하고, 체크 상태(checked)를 
            // `전체 선택` 체크 상태와 동일하게 설정함
            // 장바구니에서 체크 상태만 바꾸지 장바구니에 있는 상품의 나머지 부분을 그대로 두기
            // ...product는 product가 가진 속성들 중 그대로 보존하려는 속성들
            const updatedProducts = previous.map((product) => ({
                ...product,
                checked: isAllCheck
            }));

            // 비동기적 렌더링 문제로 수정된 updatedProducts 항목을 매개 변수로 넘겨야 정상적으로 동작합니다.
            refreshOrderTotalPrice(updatedProducts);

            return updatedProducts;
        });
    };

    // 개별 체크 박스를 클릭
    // 카트 목록(개별 선택 기능)
    const toggleCheckBox = (cartProductId: number) => {
        // 정보가 잘 가는지 간단하게 테스트하는 용도
        console.log(`카트 상품 아이디 : ${cartProductId}`);

        // previous로 인해 암시적으로 cartProducts의 이전 상태가 들어옴
        setCartProducts((previous) => {
            // !product.checked는 체크 상태를 toggle 시키는 역할
            const updatedProducts = previous.map((product) =>
                // 삼항 연산자 (조건 연산자 사용)
                // 나머지는 놔두고 check상태만 부호 반전시켜라
                product.cartProductId === cartProductId
                    ? { ...product, checked: !product.checked }
                    : product
            );

            refreshOrderTotalPrice(updatedProducts);
            return updatedProducts;
        });
    };

    // 카트 상품 목록에서 특정 상품의 구매 수량을 변경
    const changeQuantity = async (cartProductId: number, quantity: number, productId: number) => {
        // NaN : Not A Number
        if (isNaN(quantity)) { // 숫자 형식이 아니면
            // 값을 0으로 변경한 다음, 함수를 반환하도록 함
            setCartProducts((previous) => {
                // 선택한 그 카트상품을 찾아서 양을 0으로 설정하려고 반복문 돌림
                return previous.map((product) =>
                    product.cartProductId === cartProductId
                        ? { ...product, quantity: 0 }
                        : product
                );
            });

            alert('변경 수량은 최소 1이상이어야 합니다.');
            return;
        }
        try {

            // 주소 뒤에 적을 파라미터들 변수로 저장하기
            const parameter = `quantity=${quantity}&productId=${productId}`;

            // 사용 예시 : 100번 항목을 10개로 수정해주세요.
            // http://localhost:9000/cart/edit/100?quantity=10
            // 백엔드에서 @RequestParam을 사용해서 주소창에 ?quantity로 보내는 것임
            // @RequestBody로 하면 parameter로 객체 생성으로 보낼 수 있음
            const url = `${API_BASE_URL}/cart/edit/${cartProductId}?${parameter}`;

            // patch 동작은 전체가 아닌 일부 데이터를 변경하고자 할때 사용됨
            // 스프링의 WebConfig 클래스안의 addCorsMappings() 메소드를 참조하길 바람
            const response = await customAxios.patch(url, {}, {
                withCredentials: true  // ✅ 인증 정보를 함께 전송
            });

            console.log(response.data || '');

            // cartProducts의 수량 정보를 갱신
            setCartProducts((previous) => {
                const updatedProducts = previous.map((product) =>
                    product.cartProductId === cartProductId
                        ? { ...product, quantity: quantity }
                        : product
                );

                refreshOrderTotalPrice(updatedProducts);
                return updatedProducts;
            });

        } catch (error) {
            console.log('카트 상품 수량 변경 실패');
            console.log(error);
        }

    };

    // 카트 목록(삭제 함수)
    const deleteCartProduct = async (cartProductId: number) => {
        const isConfirmed = window.confirm('해당 카트 상품을 정말로 삭제하시겠습니까?');

        if (isConfirmed) {
            try {
                const url = `${API_BASE_URL}/cart/delete/${cartProductId}`;
                const response = await customAxios.delete(url);

                setCartProducts((previous) => {
                    // cartProductId : 삭제할 상품
                    // bean : 각각 한개
                    // filter : 고른거 빼고 나머지 필터링
                    const updatedProducts
                        = previous.filter((bean) => bean.cartProductId !== cartProductId);

                    return updatedProducts;
                });

                alert(response.data);

            } catch (error) {
                console.log('카트 상품 삭제 동작 오류');
                console.log(error);
            }
        } else {
            alert(`'카트 상품' 삭제를 취소하셨습니다.`);
        }
    };

    return (
        <Container className="mt-4">
            <h2 className="mb-4">
                {/* xxrem은 주위 글꼴의 xx배를 의미 */}
                <span style={{ color: 'blue', fontSize: '2rem' }}>{user?.name}</span>
                <span style={{ fontSize: '1.3rem' }}>님의 장바구니</span>
            </h2>
            <Table striped bordered>
                <thead>
                    <tr>
                        <th style={thStyle}>
                            <Form.Check
                                type="checkbox"
                                label="전체 선택"
                                // 전체 선택 "checkbox"의 onChange 이벤트 구현
                                onChange={(event) => toggleAllCheckBox(event.target.checked)}
                            />
                        </th>
                        <th style={thStyle}>상품 정보</th>
                        <th style={thStyle}>수량</th>
                        <th style={thStyle}>금액</th>
                        <th style={thStyle}>삭제</th>
                    </tr>
                </thead>
                <tbody>
                    {cartProducts.length > 0 ? (
                        cartProducts.map((product) => (
                            <tr key={product.cartProductId}>
                                <td className="text-center align-middle">
                                    <Form.Check
                                        type="checkbox"
                                        checked={product.checked}
                                        // Check박스의 값이 바뀔때마다 해당 함수 실행 (해당 매개변수)
                                        onChange={() => toggleCheckBox(product.cartProductId)}
                                    />
                                </td>
                                <td className="text-center align-middle">
                                    <Row> {/* 좌측 4칸은 이미지 영역, 우측 8칸은 상품 이름 영역 */}
                                        <Col xs={4}>
                                            <Image
                                                src={`${API_BASE_URL}/images/${product.image}`}
                                                thumbnail
                                                alt={product.name}
                                                width={`80`}
                                                height={`80`}
                                            />
                                        </Col>
                                        <Col xs={8} className="d-flex align-items-center">
                                            {product.name}
                                        </Col>
                                    </Row>
                                </td>
                                <td className="text-center align-middle">
                                    <Form.Control
                                        type="number"
                                        min={1}
                                        value={product.quantity}
                                        style={{ width: '80px', margin: '0 auto' }}
                                        // 수량 변경 입력 상자
                                        onChange={(event) =>
                                            changeQuantity(
                                                product.cartProductId,

                                                // HTML에서 모든 양식에 있는 값을 String으로 생각함
                                                // event.target.value : 방금 수정한(change)한 값(숫자여도 문자열임)
                                                // 문자열인 해당 값을 정수형으로 바꿔줌
                                                parseInt(event.target.value),
                                                product.productId
                                            )}
                                    />
                                </td>
                                <td className="text-center align-middle">
                                    {(product.price * product.quantity).toLocaleString()} 원
                                </td>
                                <td className="text-center align-middle">
                                    <Button variant="danger" size="sm"
                                        onClick={() => deleteCartProduct(product.cartProductId)}
                                    >
                                        삭제
                                    </Button>
                                </td>
                            </tr>
                        ))
                    ) : (
                        <tr><td>장바구니가 비어 있습니다.</td></tr>
                    )}
                </tbody>
            </Table>

            {/* 좌측 정렬(text-start), 가운데 정렬(text-center), 우측 정렬(text-end) */}
            {/* 5) 하단 총 주문 금액 */}
            {/* JS영역의 데이터를 사용해서 {}중괄호 사용 */}
            {/* int인 orderTotalPrice를 HTML에 사용하기 위해 toLocaleString()로 String으로 변환 */}
            <h3 className="text-end mt-3">총 주문 금액 : {orderTotalPrice.toLocaleString()}원</h3>
            <div className="text-end">
                <Button variant="primary" size="lg" >
                    주문하기
                </Button>
            </div>
        </Container>
    );
}

export default App;