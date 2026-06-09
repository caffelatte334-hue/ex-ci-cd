import { Alert, Button, Col, Container, Form, Row } from "react-bootstrap";
import type { User } from "../types/User";
import React, { useEffect, useState } from "react";

import axios from "axios";
import customAxios from './../api/axiosInstance';

import { useNavigate } from "react-router-dom";
import { API_BASE_URL } from "../config/config";

interface ProductInsertFormProps {
    user: User | null;
}

function App({ user }: ProductInsertFormProps) {
    const navigate = useNavigate();

    useEffect(() => {
        // useEffect 안에서도 ?. 문법으로 깔끔하게 줄일 수 있습니다.
        if (user?.role !== 'ADMIN') {
            alert('관리자만 접근할 수 있는 페이지입니다.');
            navigate(user ? '/' : '/member/login'); // 로그인 여부에 따라 이동지 분기
            return;
        }
    }, [user, navigate]);

    const comment = '상품 등록'; // 제목으로도 쓰고 버튼이름으로도 쓸거같아서 변수로 만든 것

    const initial_value = { // 스프링의 Entity보고 작성 (Long id / LocalDate inputdate는 스프링이 자동 생성함)
        name: '',
        price: '',
        category: '-',
        stock: '',
        image: '',
        description: '',
    };

    // 등록하고자하는 상품 정보
    // 초기 값은 initial_value
    const [product, setProduct] = useState(initial_value);

    const initialErrors = {
        name: '',
        price: '',
        category: '',
        stock: '',
        image: '',
        description: '',
        general: ''
    };

    // State를 만드는데 errors라는 이름으로 만들고 초기값은 initialErrors로 설정한다.
    const [errors, setErrors] = useState(initialErrors);

    // Change 이벤트가 발생하면 동작하는 함수
    // 폼 양식에서 어떠한 컨트롤의 값이 변경되었습니다.
    // ()는 매개변수 {}는 동작
    // HTMLInputElement 한 줄 입력상자
    // HTMLTextAreaElement 멀티라인 입력상자
    const ControlChange = (
        // HTMLInputElement : HTML부분의 input(jsx의 Control)의 요소 (한 줄 입력)
        // HTMLTextAreaElement : HTML부분의 textarea(jsx의 Control)의 요소 (여러 줄 입력)
        // HTMLSelectElement : HTML부분의 select(jsx의 Select)의 요소 (콤보박스 선택 입력)
        event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>
    ) => {
        // event.target : 해당 이벤트를 발생시킨 요소를 의미
        // event.target 객체가 가진 원소중 name과 value인 키의 값만 골라서 변수로 지정함
        // event.target.name : Form.Control의 속성인 name의 값
        // event.target.value : 사용자가 입력한 값
        const { name, value } = event.target;

        // Product가 객체여서 중괄호 사용
        // {[name]: value}만 적으면 name을 제외한 나머지 속성들이 휘발됨
        // 이를 방지하기 위해 ...product를 추가 (...product는 기존 product의 값을 가져오는 것)
        // ...product로 가져온 기존의 [name]의 value는 내가 새로 적은 value로 덮어쓰기 됨
        // (...은 전개 연산자로 불림 - 예약어) (...객체 or 배열 : 객체 or 배열의 원소들을 가져오는 문법)
        setProduct({ ...product, [name]: value });
    };

    const FileSelect = (
        event: React.ChangeEvent<HTMLInputElement>
    ) => {
        // event.target 객체가 가진 원소중 name과 files인 키의 값만 골라서 변수로 지정함
        // files가 첨부 파일(이미지)의 value라고 생각하면 됨
        const { name, files } = event.target;

        // 입력한 값이 파일(이미지)이 아닐때의 조건식과 동작
        // !files : 입력한 값이 파일이 아닐때 값이 비어있음
        // files.length === 0 : 값 자체는 존재하는데 기본값인 0일때 - 사실상 존재하지 않음
        if (!files || files.length === 0) {
            alert('이미지 파일을 선택해 주셔야 합니다.');
            return;
        }
        // 1) JS에서 만약 체크 박스가 있다고 할 때 2개를 체크하면 배열이 만들어짐
        // 2) 이미지 선택 양식을 의미하는 배열
        // 3) type이 file인 이미지는 선택하면 1개여도 객체 데이터 덩어리여서 무조건 배열로 생성이 됨
        // 파일을 선택하면 배열의 앞 부분에 추가됨
        // 4) files[0] : 이미지를 여러개 선택해도 우리는 이미지 1개가 필요해서
        // 제일 첫번째 이미지 오직 한개!를 선택하기 위해서
        // files배열의 인덱스 0번인 files[0]를 file에 넣은 것
        const file = files[0]; // type="file"로 작성한 첫번째 항목

        // FileReader : 이미지 파일(바이너리 바이트 데이터)을 읽어 들여서
        // JS가 알아듣게 텍스트 문장으로 변환해 주는 번역기
        const reader = new FileReader();

        // readAsDataURL() : 해당 이미지를 Base64 인코딩 문자열 형식으로 변환해서 reader 객체에 저장함
        // 사용 예시 : data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUA...
        // 스프링에서 이 텍스트를 받고나서
        // 필요한 부분은 ,뒤에 있는 iVBORw0KGgoAAAANSUhEUgAAAAUA...이 부분이여서
        // String 클래스의 substring으로 추출해와야함
        reader.readAsDataURL(file);

        // readAsDataURL()은 시간이 걸리는 비동기 작업이라 끝났을때 알려주는 함수가 필요함
        // JS가 이해할 수 있도록 변경해주는 과정이 다 끝날을때 하는 동작을 지정하는 함수
        reader.onloadend = () => {
            // result안에는 data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUA...
            // 이 값이 들어 있음
            const result = reader.result;

            // [name]는 name 변수의 값(image)
            setProduct({ ...product, [name]: result });
        }
    };

    const SubmitAction = async (event: React.SyntheticEvent<HTMLFormElement>) => {
        // 원래 취해야 할 동작을 못하게 하기
        event.preventDefault();

        if (product.category === '-') {
            alert('상품 카테고리는 반드시 선택해 주셔야 합니다.');
            return;
        }

        try {
            const url = `${API_BASE_URL}/product/insert`;
            const config = {
                headers: { 'Content-Type': 'application/json' }
            };

            const response = await customAxios.post(url, product, config);

            console.log('응답 데이터 : ');
            console.log(`${response.data}`);

            alert('상품 등록되었습니다.');

            // 초기화 하기
            // 강제로 '/product/list'로 이동해서 초기화하지 않아도 되지만
            // 뒤로가기 버튼을 눌렀을때 입력했던 데이터가 남아있으니까 초기화 함
            setProduct(initial_value);
            setErrors(initialErrors);

            navigate('/product/list');

        } catch (error: unknown) {
            console.log(error);
            if (axios.isAxiosError(error) && error.response) {
                // 백엔드에서 전달받은 오류 메시지를 저장
                setErrors((prev) => ({
                    ...prev,
                    ...error.response?.data?.errors,
                    general: error.response?.data?.message || '상품 등록 중 오류가 발생했습니다.'
                }));
            } else {
                setErrors((prev) => ({
                    ...prev,
                    general: '서버와의 통신 중 오류가 발생했습니다.'
                }));
            }
        };
    };

    return (
        <Container style={{ marginTop: '30px' }}>
            <h1>{comment}</h1>

            {/* 일반 오류 메시지 */}
            {errors.general && <Alert variant="danger">{errors.general}</Alert>}

            {/* 특별한 말이 없으면 Form을 불러올때 bootstrap으로 하면 됨 */}
            <Form onSubmit={SubmitAction}> {/* id는 자동 생성하게 스프링에 만들어 놓아서 입력란에 넣을 필요는 없음 */}

                {/* 이름 입력창 */}
                {/* controlId="formName" 이건 필수는 아님 */}
                <Form.Group as={Row} className="mb-3" controlId="formName">
                    <Form.Label column sm={2}>
                        이름
                    </Form.Label>
                    <Col sm={10}> {/* Form.Control은 HTML의 form의 input같은 것 */}
                        <Form.Control
                            type="text"
                            placeholder="이름을(를) 입력해 주세요."

                            // 정확히 말하자면 name 속성이 아니고 id속성임
                            // 그래서 Form.Group태그의 controlId속성에 formName으로 설정함
                            // name이 price면 price로 바꾸고 formPrice로 하면 됨
                            name="name"
                            value={product.name}

                            // Change 이벤트 : 값이 변하면 동작하는 이벤트
                            onChange={ControlChange}

                            // 값을 정확하게 boolean 타입으로 만들어서 true나 false로 만들려고 !!사용
                            isInvalid={!!errors.name}
                        />

                        {/* 문제가 생기면 나오는 경고성 멘트 */}
                        <Form.Control.Feedback type="invalid">
                            {errors.name}
                        </Form.Control.Feedback>
                    </Col>
                </Form.Group>

                {/* 가격 입력창 */}
                <Form.Group as={Row} className="mb-3" controlId="formPrice">
                    <Form.Label column sm={2}>
                        가격
                    </Form.Label>
                    <Col sm={10}> {/* Form.Control은 HTML의 form의 input같은 것 */}
                        <Form.Control
                            type="number"
                            placeholder="가격을(를) 입력해 주세요."

                            // 정확히 말하자면 name 속성이 아니고 id속성임
                            name="price"
                            value={product.price}

                            // Change 이벤트 : 값이 변하면 동작하는 이벤트
                            onChange={ControlChange}

                            // 값을 정확하게 boolean 타입으로 만들어서 true나 false로 만들려고 !!사용
                            isInvalid={!!errors.price}
                        />

                        {/* 문제가 생기면 나오는 경고성 멘트 */}
                        <Form.Control.Feedback type="invalid">
                            {errors.price}
                        </Form.Control.Feedback>
                    </Col>
                </Form.Group>

                {/* 카테고리 입력창 */}
                {/* Form.Control말고 select로 콤보 박스 만들기 */}
                {/* 스프링의 constant폴더의 Category.java인 이용해서 */}
                {/* type, placeholder 삭제 */}
                <Form.Group as={Row} className="mb-3" controlId="formCategory">
                    <Form.Label column sm={2}>
                        카테고리
                    </Form.Label>
                    <Col sm={10}> {/* Form.Select는 HTML의 form의 select같은 것 */}
                        <Form.Select

                            // 정확히 말하자면 name 속성이 아니고 id속성임
                            name="category"
                            value={product.category}

                            // Change 이벤트 : 값이 변하면 동작하는 이벤트
                            onChange={ControlChange}
                            isInvalid={!!errors.category}
                        >
                            <option value="-">-- 상품 카테고리를 선택해 주세요.</option>
                            <option value="BREAD">빵</option>
                            <option value="BEVERAGE">음료수</option>
                            <option value="CAKE">케이크</option>
                            <option value="MACARON">마카롱</option>
                        </Form.Select>

                        {/* 문제가 생기면 나오는 경고성 멘트 */}
                        <Form.Control.Feedback type="invalid">
                            {errors.category}
                        </Form.Control.Feedback>
                    </Col>
                </Form.Group>

                {/* 재고 입력창 */}
                <Form.Group as={Row} className="mb-3" controlId="formStock">
                    <Form.Label column sm={2}>
                        재고
                    </Form.Label>
                    <Col sm={10}> {/* Form.Control은 HTML의 form의 input같은 것 */}
                        <Form.Control
                            type="number"
                            placeholder="재고 수량은 10개 이상 입력해 주셔야 합니다."

                            // 정확히 말하자면 name 속성이 아니고 id속성임
                            name="stock"
                            value={product.stock}

                            // Change 이벤트 : 값이 변하면 동작하는 이벤트
                            onChange={ControlChange}

                            // 값을 정확하게 boolean 타입으로 만들어서 true나 false로 만들려고 !!사용
                            isInvalid={!!errors.stock}
                        />

                        {/* 문제가 생기면 나오는 경고성 멘트 */}
                        <Form.Control.Feedback type="invalid">
                            {errors.stock}
                        </Form.Control.Feedback>
                    </Col>
                </Form.Group>

                {/* 이미지 입력창 */}
                {/* type을 text가 아니라 file로 바꾸기 */}
                {/* onChange={FileSelect}로 바꾸기 */}
                {/* placeholder, value 삭제 */}
                <Form.Group as={Row} className="mb-3" controlId="formImage">
                    <Form.Label column sm={2}>
                        이미지
                    </Form.Label>
                    <Col sm={10}> {/* Form.Control은 HTML의 form의 input같은 것 */}
                        <Form.Control
                            type="file"

                            // 정확히 말하자면 name 속성이 아니고 id속성임
                            name="image"

                            // Change 이벤트 : 값이 변하면 동작하는 이벤트
                            // file은 값이 아니고 객체(물건)으로 취급해서 다르게 해야 함
                            onChange={FileSelect}

                            // 값을 정확하게 boolean 타입으로 만들어서 true나 false로 만들려고 !!사용
                            isInvalid={!!errors.image}
                        />

                        {/* 문제가 생기면 나오는 경고성 멘트 */}
                        <Form.Control.Feedback type="invalid">
                            {errors.image}
                        </Form.Control.Feedback>
                    </Col>
                </Form.Group>

                {/* 설명 입력창 */}
                <Form.Group as={Row} className="mb-3" controlId="formDescription">
                    <Form.Label column sm={2}>
                        상품 설명
                    </Form.Label>
                    <Col sm={10}> {/* Form.Control은 HTML의 form의 input같은 것 */}
                        <Form.Control
                            type="text"
                            placeholder="상품 설명을(를) 입력해 주세요."

                            // 정확히 말하자면 name 속성이 아니고 id속성임
                            name="description"
                            value={product.description}

                            // Change 이벤트 : 값이 변하면 동작하는 이벤트
                            onChange={ControlChange}

                            // 값을 정확하게 boolean 타입으로 만들어서 true나 false로 만들려고 !!사용
                            isInvalid={!!errors.description}
                        />

                        {/* 문제가 생기면 나오는 경고성 멘트 */}
                        <Form.Control.Feedback type="invalid">
                            {errors.description}
                        </Form.Control.Feedback>
                    </Col>
                </Form.Group>

                <Button variant="primary" type="submit" size="lg">
                    {comment}
                </Button>

            </Form>
        </Container>

    );
};

export default App;