import { useState } from "react";
import { Alert, Button, Card, Col, Container, Form, Row } from "react-bootstrap";
import { Link, useNavigate } from "react-router-dom";

import axios from "../api/axiosInstance.tsx";
import type { LoginResponse, User } from "../types/User";

interface Props {
    // App.tsx -> AppRoutes.tsx를 거쳐온 프롭스(정보가 들어오면 App.tsx에 데이터를 보내야함)
    // onLogin 프롭스는 User 형식으로 매개 변수를 받고, 반환 타입이 없습니다.
    onLogin: (user: User) => void;
}

function App({ onLogin }: Props) { // 프롭스를 매개변수에 넣어서 사용할 수 있게 함
    // 이 문서내에서 바뀔 소지가 있는 것들은 state로 만들어 관리 할 수 있음
    // props는 부모에게서! 받은거고 / state는 자신의 문서 내에서! 있는 것들이고
    // 로그인과 관련된 state (로그인에 필요해서 스프링에서 정보 대응시킴)
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');

    // 에러 관련 메시지
    const [errors, setErrors] = useState('');

    const navigate = useNavigate();

    const handleLogin = async (event: React.SubmitEvent) => {
        event.preventDefault(); // 새로고침 방지
        console.log('로그인 시도중입니다.');

        try {
            const url = '/member/login';
            const params = { email, password }; // 파라미터
            const config = {
                headers: { // 헤더에 MIME type 적어서 요청
                    "Content-Type": "application/json"
                }
            };
            // 요청해서 가져올 데이터를 LoginResponse 형식으로 요청함
            const response = await axios.post<LoginResponse>(url, params, config);

            console.log('응답 데이터 : \n' + response.data);

            // 서버의 응답을 전개 연산자로 처리합니다.
            // accessToken는 JWT, userData는 User.ts으로 구성된 객체
            // accessToken은 변수로 가져오고 ...userData는 객체로 가져옴
            const { accessToken, ...userData } = response.data;

            // localStorage에는 문자열만 들어갈 수 있음
            // 전개 연산자로 변수로 가져온 accessToken은 바로 넣을 수 있음
            localStorage.setItem("accessToken", accessToken);

            console.log('로그인 성공 사용자 : ' + userData);

            // 함수를 조건식에 넣는 것은 존재 유무를 판별하려고
            // (프롭스로 진짜 받아온 함수인가정도를 판단함)
            if (onLogin) {
                onLogin(userData);

                // userData는 자바스크립트 객체여서 문자열로 바꿔줘야 함
                // JSON.stringify 함수는 JavaScript 객체를 JSON 문자열로 변환해 줍니다.
                // App.tsx에서 로컬스토리지에 저장할 예정이라 여기서는 안해도 됨
                localStorage.setItem("user", JSON.stringify(userData));
            }

            // 로그인이 되면 메인 홈페이지로 이동시킴
            navigate("/");

        } catch (error: any) {
            if (error.response) { // 서버가 에러 응답을 보냈을때
                // 백엔드에서 작성한 에러 메시지
                setErrors(error.response.data.message || "로그인 실패");
            } else { // 서버가 에러 응답을 안보냈을때 - 네트워크 문제
                setErrors("Server Error");
            }
        }
    };

    console.log('자바스크립트 코딩 영역');

    return (
        <Container fluid className="d-flex justify-content-center align-items-center" style={{ height: "70vh" }}>
            <Row className="w-100 justify-content-center">
                <Col md={6} sm={10}>
                    <Card>
                        <Card.Body>
                            <h2 className="text-center mb-4">로그인</h2>

                            {errors && <Alert variant="danger">{errors}</Alert>}

                            <Form onSubmit={handleLogin}>
                                <Form.Group as={Row} className="mb-3 align-items-center">
                                    <Form.Label column sm={3} className="text-end fw-bold text-primary">
                                        이메일
                                    </Form.Label>
                                    <Col sm={9}>
                                        <Form.Control
                                            type="email"
                                            placeholder="이메일을 입력해 주세요."
                                            value={email}
                                            onChange={(e) => setEmail(e.target.value)}
                                            required
                                        />
                                    </Col>
                                </Form.Group>

                                <Form.Group as={Row} className="mb-3 align-items-center">
                                    <Form.Label column sm={3} className="text-end fw-bold text-primary">
                                        비밀 번호
                                    </Form.Label>
                                    <Col sm={9}>
                                        <Form.Control
                                            type="password"
                                            placeholder="비밀 번호을 입력해 주세요."
                                            value={password}
                                            onChange={(e) => setPassword(e.target.value)}
                                            required
                                        />
                                    </Col>
                                </Form.Group>

                                <Row className="g-2">
                                    <Col xs={8}>
                                        <Button variant="primary" type="submit" className="w-100">
                                            로그인
                                        </Button>
                                    </Col>
                                    <Col xs={4}>
                                        <Link to="/member/signup" className="btn btn-outline-secondary w-100">
                                            회원 가입
                                        </Link>
                                    </Col>
                                </Row>
                            </Form>

                        </Card.Body>
                    </Card>
                </Col>
            </Row>
        </Container>
    );
};

export default App;