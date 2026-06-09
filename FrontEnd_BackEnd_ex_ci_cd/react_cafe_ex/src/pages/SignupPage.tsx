import axios from "axios";
import { useState } from "react";
import { Card, Container, Row, Form, Col, Button, Alert } from "react-bootstrap";
import { API_BASE_URL } from "../config/config";
import { useNavigate } from "react-router-dom";

function App() {
    // 회원 가입시 필요한 항목들을 state로 정의하기
    // id는 자동생성 / Role과 LocalDate는 유저가 회원가입할때 마음대로 설정하면
    // 문제가 되는 부분이여서 Service에서 따로 정의
    // 파라미터 관련 state 변수 선언
    const [name, setName] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [address, setAddress] = useState('');


    // 폼 유효성 검사(Form Validation Check) 관련 state 정의 : 입력 양식에 문제 발생시 값을 저장할 곳
    // general은 그냥 일반 오류가 있을까봐 적어 놓은 것 (큰 의미 X)
    const [errors, setErrors] = useState({
        name: '', email: '', password: '', address: '', general: ''
    });

    // useNavigate() : 실행시 페이지를 이동시키는 기능을 가진 함수를 반환함
    // const navigate : 반환된 함수를 navigate라는 이름의 변수에 담아두는 것
    // 자바스크립트에서 함수는 객체(값)고, 이 함수를 변수에 담을 수 있음
    // 따라서 페이지를 이동시키는 함수라는 객체(함수)를 navigate라는 변수에 담아서 사용이 가능함
    // ex) navigate('/member/login');
    const navigate = useNavigate();

    // "event: React.SubmitEvent"는 자바로 따지면 React.SubmitEvent event
    // 즉 React.SubmitEvent라는 타입(클래스or인터페이스)의 event라는 변수(객체)라는 뜻
    // 이것을 매개변수 자리에 넣어서 React.SubmitEvent(클래스or인터페이스)의 함수(preventDefault())를 쓸 수 있게 해줌
    // async : 해당 함수를 비동기식으로 만드는 예약어
    const SingupAction = async (event: React.SubmitEvent) => {
        // preventDefault() : 브라우저에 내장된 기본 이벤트 리스너의 동작을 강제로 취소시킴
        // 즉, 브라우저가 멋대로 새로고침같은 행동을 못하게 막는 것 = 이벤트 전파 방지
        event.preventDefault(); // 이벤트 전파 방지

        try {
            // 요청을 보낼 주소 (${API_BASE_URL} = 스프링 - 백엔드 주소)
            const url = `${API_BASE_URL}/member/signup`;
            // 변수 url 주소인 백엔드에 보낼 데이터 덩어리
            const parameters = { name, email, password, address };
            // 백엔드와 통신시 쿠키나 세션 정보를 안전하게 공유하겠다는 설정
            const config = { withCredentials: true };
            // 변수 url 주소인 백엔드에 parameters들을 보내는데 post방식으로 보냄
            const response = await axios.post(url, parameters, config);

            if (response.status === 200) { /* 스프링의 MemberController 파일 참조 */
                alert('회원 가입 성공');
                navigate('/member/login');
            }

        } catch (error) { // error : 예외 객체 (백엔드에서 응답할때 에러시 반송하는데 그때 axios 통신 라이브러리에서 자동으로 만들어서 던져줌)
            // 조건식 : 발생한 에러가 네트워크 통신(Axios)중에 생긴 에러가 맞는지
            if (axios.isAxiosError(error)) {
                if (error.response?.data) {
                    // 서버에서 받은 오류 정보를 객체로 저장합니다.
                    setErrors(error.response.data);
                }
            } else { // 입력 값 이외에 발생하는 다른 오류과 관련됨
                // ...prev는 해당 객체에 다른 데이터들은 그전과 동일한 값으로 지정한다는 설정
                setErrors((prev) => ({
                    ...prev,
                    general: "회원 가입 중에 오류가 발생하였습니다.",
                }));
            }
        }
    };

    return (
        <Container className="d-flex justify-content-center align-items-center" style={{ height: '70vh' }}>
            <Row className="w-100 justify-content-center">
                <Col md={6}>
                    <Card>
                        <Card.Body>
                            <h2 className="text-center mb-4">회원 가입</h2>

                            {/* 일반 오류 발생시 사용자에게 alert 메시지를 보여 줍니다. */}
                            {/* contextual : 상황에 맞는 적절한 스타일 색상을 지정하는 기법 */}
                            {errors.general && <Alert variant="danger">{errors.general}</Alert>}

                            {/*
                                !! 연산자는 어떠한 값을 강제로 boolean 형태로 변환해주는 자바스크립트 기법입니다.

                                isInvalid 속성은 해당 control의 유효성을 검사하는 속성입니다.
                                값이 true이면 Form.Control.Feedback에 빨간 색상으로 오류 메시지를 보여 줍니다.
                            */}

                            {/* 폼 제출 시(type이 submit인 버튼 클릭시) SingupAction함수가 실행되게 함 */}
                            <Form onSubmit={SingupAction}>
                                {/* 이름 */}
                                <Form.Group as={Row} className="mb-3"> {/* as는 자격임 Row의 자격으로써 이 구문을 보겠다. */}
                                    <Form.Label column sm={3}> {/* grid에서 3칸 */}
                                        이름
                                    </Form.Label>

                                    {/* HTML의 input이 JSX에서는 Control*/} {/* grid에서 9칸 */}
                                    <Col sm={9}>
                                        {/* 속성들 적기 */}
                                        {/* value={name} 이 태그의 값과 내가 원하는 변수?와 연결 */}
                                        {/* onChange={(e) => setName(e.target.value)} 이 태그의 값이 바뀌면 연결된 변수도 값이 변함 */}
                                        <Form.Control
                                            type="text"
                                            placeholder="이름을 입력해 주세요."
                                            value={name}
                                            // 입력창에 글자가 추가되거나 삭제되는 등 값이 변할때마다 실행되는 이벤트 핸들러
                                            // (키보드를 한 번 누를때마다 실행됨)
                                            // e : 이벤트 (입력창 값이 변함)가 발생하면 그 이벤트에 대한 정보를 매개변수인 e(내가 임의로 설정)에 담아줌
                                            // e.target : 이벤트가 발생한 요소 (입력창의 변화) / e.target.value : 입력창의 변화의 값 -> setName으로 세터
                                            onChange={(e) => setName(e.target.value)}
                                            // 값({!!errors.name})이 true : errors.name의 값이 존재하는 것 -> 에러가 있다는 것
                                            // 값({!!errors.name})이 false : errors.name의 값이 존재하지 않는 것 -> 에러가 없다는 것 : 
                                            isInvalid={!!errors.name}
                                        />
                                        <Form.Control.Feedback type="invalid">
                                            {/* errors는 백엔드에 의해 (response.data) 세터되는 데이터여서 백엔드에 에러 문구가 설정되어 있음 */}
                                            {errors.name}
                                        </Form.Control.Feedback>
                                    </Col>
                                </Form.Group>


                                {/* 이메일 */}
                                <Form.Group as={Row} className="mb-3">
                                    <Form.Label column sm={3}>
                                        이메일
                                    </Form.Label>
                                    <Col sm={9}>
                                        <Form.Control
                                            type="text"
                                            placeholder="이메일을 입력해 주세요."
                                            value={email}
                                            onChange={(e) => setEmail(e.target.value)}
                                            isInvalid={!!errors.email}
                                        />
                                        <Form.Control.Feedback type="invalid">
                                            {errors.email}
                                        </Form.Control.Feedback>
                                    </Col>
                                </Form.Group>

                                {/* 비밀번호 */}
                                <Form.Group as={Row} className="mb-3">
                                    <Form.Label column sm={3}>
                                        비밀번호
                                    </Form.Label>
                                    <Col sm={9}>
                                        <Form.Control
                                            type="password"
                                            placeholder="비밀 번호를 입력해 주세요."
                                            value={password}
                                            onChange={(e) => setPassword(e.target.value)}
                                            isInvalid={!!errors.password}
                                        />
                                        <Form.Control.Feedback type="invalid">
                                            {errors.password}
                                        </Form.Control.Feedback>
                                    </Col>
                                </Form.Group>

                                {/* 주소 */}
                                <Form.Group as={Row} className="mb-3">
                                    <Form.Label column sm={3}>
                                        주소
                                    </Form.Label>
                                    <Col sm={9}>
                                        <Form.Control
                                            type="text"
                                            placeholder="주소를 입력해 주세요."
                                            value={address}
                                            onChange={(e) => setAddress(e.target.value)}
                                            isInvalid={!!errors.address}
                                        />
                                        <Form.Control.Feedback type="invalid">
                                            {errors.address}
                                        </Form.Control.Feedback>
                                    </Col>
                                </Form.Group>

                                <Button variant="primary" type="submit" className="w-100">
                                    회원 가입
                                </Button>


                            </Form>
                        </Card.Body>
                    </Card>
                </Col>
            </Row>
        </Container >
    );
};

export default App;