import { Button, Card, Col, Container, Row } from "react-bootstrap";
import { useEffect, useState } from "react";
import type { Product } from "../types/Product";

import customAxios from './../api/axiosInstance';

import { API_BASE_URL } from "../config/config";
import type { User } from "../types/User";
import { Link, useNavigate } from "react-router-dom";
import axios from "axios";

// 넘어온 user를 위한 ProductProps type를 정의
type ProductProps = { // user: User는 자바의 객체: 클래스 정도로 이해하면 됨
    user: User | null; // 로그인하면 의미 있는 객체, 아니면 null (로그인하면 App.tsx부터 정보를 넣고 내려옴)
};


function App({ user }: ProductProps) { // 프롭스 주입
    const [products, setProducts] = useState<Product[]>([]);

    useEffect(() => {
        const url = `${API_BASE_URL}/product/list`;
        customAxios.get(url)
            .then((response) => {
                console.log('응답 받은 데이터');
                console.log(response);
                setProducts(response.data);
            })
            .catch((error) => {
                console.log(error);
            });
    }, []);

    const navigate = useNavigate();

    const makeAdminButtons = (item: Product, user: User | null, navigate: any) => {
        if (user?.role !== 'ADMIN') return null;

        return (
            <div className="d-flex justify-content-center">
                <Button // 수정을 위한 <Button>을 추가합니다.
                    variant="warning"
                    className="mb-2"
                    size="sm"
                    onClick={(event) => {
                        // 이 코드가 없으면 수정버튼을 포함하고 있는 더 큰 영역을 클릭했을때
                        // 그 큰 영역도 onClick으로 다른 일을 해야하는데 그 일과 이 코드가 동시에 시작해버려서
                        // 문제가 생길 수도 있음
                        event.stopPropagation(); // 이벤트 버블링 방지
                        navigate(`/product/update/${item.id}`);
                    }}>
                    수정
                </Button>

                {/* 한 칸 공백 넣기 */}
                &nbsp;

                <Button // 삭제를 위한 <Button>을 추가합니다. (confirm 함수 이용)(alert과는 다름)
                    variant="danger"
                    className="mb-2"
                    size="sm"
                    onClick={async (event) => { // 백엔드를 거치고 일처리를 해야해서 async 붙임
                        event.stopPropagation(); // 이벤트 버블링 방지

                        const isDelete = window.confirm(`${item.name} 상품을 삭제하시겠습니까?`);

                        if (isDelete === false) {
                            /* sweet alert2 사이트에 이쁜거 많음 */
                            alert(`${item.name} 상품 삭제를 취소하였습니다.`)
                            return;
                        }

                        try { // 전체 배열에서 일부 데이터만 필터할 수 있음
                            const url = `${API_BASE_URL}/product/delete/${item.id}`;
                            await axios.delete(url); // 백엔드를 거치고 일처리를 해야해서 await 붙임
                            alert(`'${item.name}' 상품이 삭제되었습니다.`)

                            // 상품을 갱신해주는 setter
                            // 이전(prev) : 기존 state (삭제 전)
                            // filter() : true인 것만 따로 모아서 새로운 배열을 생성
                            // p : prev에 있는 기존 상품들
                            // p.id !== item.id : 기존 상품의 id와 삭제된 상품의 id가 다르다 -> 삭제된 상품이 아니다.
                            // 삭제된 상품이 아닌 것들만 따로 모아서 다시 products에 저장
                            setProducts(prev => prev.filter(p => p.id !== item.id));

                            navigate('/product/list');

                        } catch (error) {
                            console.log(error);
                            if (axios.isAxiosError(error)) {
                                alert(`상품 삭제 실패 : ${error.response?.data || error.message}`);
                            } else {
                                console.log('알수 없는 에러 : ' + error);
                            }
                        };
                    }}>
                    삭제
                </Button>
            </div>
        );
    };

    console.log('자바스크립트 코딩 영역');

    return (
        <Container className="my-4">
            <h1 className="my-4">상품 목록 페이지</h1>

            {/* 상품 등록을 위한 <Link>를 추가합니다. */}
            <Link to={`/product/insert`}>
                {/* 수정, 삭제를 위한 <Button>을 추가합니다. */}
                {/* 조건 && (HTML) 문법은 "조건이 맞을 때만 우측의 HTML을 보여달라"는 뜻 */}
                {user?.role === 'ADMIN' && (
                    <Button variant="primary" className="mb-3">
                        상품 등록
                    </Button>
                )}
            </Link>


            {/* 필드 검색 영역 */}

            {/* 자료 보여 주는 영역 */}
            <Row>
                {/* products는 상품 배열, item는 상품 1개를 의미 */}
                {products.map((item) => ( // 자바의 확장 for문과 같은 리액트 문법
                    <Col key={item.id} md={4} className="mb-4">
                        <Card className="h-100"
                            onClick={() => navigate(`/product/detail/${item.id}`)} // 상세보기로 이동하기 위한 속성 추가
                            style={{ cursor: 'pointer' }}>
                            <Card.Img
                                variant="top"
                                src={`${API_BASE_URL}/images/${item.image}`}
                                alt={item.name}
                                style={{ width: '100%', height: '200px' }}
                            />
                            <Card.Body>
                                <table style={{ width: '100%', borderCollapse: 'collapse', border: 'none' }}>
                                    <tbody>
                                        <tr>
                                            <td style={{ width: '70%', padding: '4px', border: 'none' }}>
                                                <Card.Title>{item.name}({item.id})</Card.Title>
                                            </td>
                                            <td rowSpan={2} style={{ padding: '4px', border: 'none', textAlign: 'center', verticalAlign: 'middle' }}>
                                                {makeAdminButtons(item, user, navigate)}    
                                            </td>
                                        </tr>
                                        <tr>
                                            <td style={{ width: '70%', padding: '4px', border: 'none' }}>
                                                <Card.Text>가격 : {item.price.toLocaleString()} 원</Card.Text>
                                            </td>
                                        </tr>
                                    </tbody>
                                </table>

                            </Card.Body>
                        </Card>
                    </Col>
                ))}
            </Row>

            {/* 페이징 처리 영역 */}

        </Container>
    );
};

export default App;