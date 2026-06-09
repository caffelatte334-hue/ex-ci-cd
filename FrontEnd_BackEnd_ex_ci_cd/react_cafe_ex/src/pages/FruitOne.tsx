import axios from "axios"

import { useEffect, useState } from "react";
import { API_BASE_URL } from "../config/config";
import { Table } from "react-bootstrap";

import type { Fruit } from "../types/Fruit"

// axios 라이브러리를 이용하여 리액트에서 스프링으로 데이터를 요청함
function App() {
    // useState : 파일 내에서 변할 가능성이 있는 변수를 state 변수로 설정해서 관리함
    // state 변수는 오직 setState 함수에 의해서만 값이 변화함
    // 문법 : const [state변수, state변경함수] = useState<타입>(초기값);
    // 처음에는 값이 없으므로 null, 데이터가 들어 오면 Fruit 타입
    const [fruit, setFruit] = useState<Fruit | null>(null);

    // useEffect(() => {}, []); : 컴포넌트가 화면에 나타날 때 특정 작업을 자동으로 실행하게 만드는 도구
    // []가 비어있으면 오직 한번 실행한다는 뜻 / [] 안에 매개변수가 있으면 그 변수의 값이 변화할때마다 새로 렌더링함
    useEffect(() => {
        // BackEnd 서버에서 데이터 읽어 오기
        // async (비동기식) : 시간이 걸리는 작업 (서버 요청 등)을 시작만 해두고 끝나길 기다리지 않고
        // 다음 코드로 넘어가게 하는 방식
        const fetchResult = async () => {
            try { // 백엔드에 요청하는 내용 작성
                // 요청할 url 주소 / config.tsx에서 변수 가져와야해서 import 함
                const url = `${API_BASE_URL}/fruit`;

                // 요청을 보낼때 인증 정보를 가진 쿠키도 같이 요청하기 위해 만든 변수
                const config = { withCredentials: true };

                // 스프링에서 /fruit가 RestController로 되어있어서 JSON파일로 받아짐
                // <Fruit>라는건 Fruit.ts와 똑같은 형식의 데이터를 해당 url에 가서 찾으라는 말
                // 스프링의 @GetMapping("/fruit")된 곳인 FruitController의 경우
                // bean이라는 Fruit클래스의 객체가 이에 해당함

                // await (async와 짝꿍) : async로 다음 코드로 원래는 넘어가지만 await 부분에서는
                // async의 결과가 나올때까지 기다리고 그 다음줄이 실행되게 함
                // axios의 get방식으로 메핑된 url에 가서 Fruit와 동일한 형식의 데이터의 값을 가져와서
                // response에 넣기
                const response = await axios.get<Fruit>(url, config);

                // fruit라는 State변수에 스프링에서 가져온 데이터인 response의 데이터를 넣음
                setFruit(response.data);
            } catch (error) { // 예외처리할때 사용하는 구문 (오류처리)
                console.log(error);
            }
        };

        fetchResult(); // 직접 호출
    }, []);

    return (
        <>
            <Table hover style={{ margin: '20px' }}>
                <tbody>
                    <tr>
                        <td>아이디</td>

                        {/* optional chaining : fruit가 null → 아무것도 안 나옴(undefined 반환), fruit가 존재 → id 출력 */}
                        {/* JSX영역에 JS의 변수, 객체, 함수등을 사용할때 {}안에 넣고 JS와 같은 문법으로 사용하면 됨 */}
                        {/* 그냥 적으면 문자열로 인식함 */}
                        {/* 백엔드에서 준 데이터인 response.data로 setFruit해서 값이 변화된 state 변수인 fruit의 id를 사용 */}
                        <td>{fruit?.id}</td>
                    </tr>
                    <tr>
                        <td>상품명</td>
                        {/* 백엔드에서 준 데이터인 response.data로 setFruit해서 값이 변화된 state 변수인 fruit의 name을 사용 */}
                        <td>{fruit?.name}</td>
                    </tr>
                    <tr>
                        <td>단가</td>
                        {/* 백엔드에서 준 데이터인 response.data로 setFruit해서 값이 변화된 state 변수인 fruit의 price를 사용 */}
                        {/* toLocaleString은 사용자 지역의 숫자 단위에 맞게 ,등이 생김 */}
                        {/* 원래 10000원이라는 문장을 10,000원으로 바꿔줌 */}
                        <td>{fruit?.price.toLocaleString()}원</td>
                    </tr>
                </tbody>
            </Table >
        </>
    );
}

export default App;