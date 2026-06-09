import { useEffect, useState } from "react";
import type { Fruit } from "../types/Fruit";
import axios from "axios";
import { API_BASE_URL } from "../config/config";
import { Table } from "react-bootstrap";


function App() {
  // 문법 : const [state변수, state변경함수] = useState<타입>(초기값);
  // FruitOne과 같이 Fruit.ts를 가져오지만 Fruit.ts 타입으로 된 변수가 여러개여서
  // Fruit[]인 배열로 만들어서 가져오기
  // 배열은 배열 안에 아무런 데이터가 없어도 오류가 나지 않아서 | null을 넣지 않아도 됨 (오히려 넣으면 오류가 날 수도 있음)
  const [fruitList, setFruitList] = useState<Fruit[]>([]);

  // useEffect(() => {}, []); : 컴포넌트가 화면에 나타날 때 특정 작업을 자동으로 실행하게 만드는 도구
  // []가 비어있으면 오직 한번 실행한다는 뜻 / [] 안에 매개변수가 있으면 그 변수의 값이 변화할때마다 새로 렌더링함
  useEffect(() => {
    // BackEnd 서버에서 데이터 읽어 오기
    // async (비동기식) : 시간이 걸리는 작업 (서버 요청 등)을 시작만 해두고 끝나길 기다리지 않고
    // 다음 코드로 넘어가게 하는 방식
    const fetchData = async () => {
      try { // 백엔드에 요청하는 내용 작성
        // 요청할 url 주소 / config.tsx에서 변수 가져와야해서 import 함
        const url = `${API_BASE_URL}/fruit/list`;

        // await (async와 짝꿍) : async로 다음 코드로 원래는 넘어가지만 await 부분에서는
        // async의 결과가 나올때까지 기다리고 그 다음줄이 실행되게 함
        // axios의 get방식으로 메핑된 url에 가서 Fruit와 동일한 형식의 데이터의 값을 가져와서
        // response에 넣기
        const response = await axios.get<Fruit[]>(url);
        setFruitList(response.data);

      } catch (error) {
        console.log(error);
      }
    }
    fetchData(); // 직접 호출
  }, []);

  return (
    <>
      <Table hover style={{ margin: '20px' }}>
        <thead>
          <tr>
            <th>아이디</th>
            <th>상품명</th>
            <th>단가</th>
          </tr>
        </thead>
        <tbody> {/* fruitList라는 state는 배열이여서 배열 안에 있는 요소를 꺼내기 위해 map함수 사용 */}
          {fruitList.map((fruit) => // map((매개변수명) => ) // map은 여러가지 요소중 데이터를 특정하기 위해서 key가 필수
            <tr key={fruit.id}> {/* fruit.id를 기준으로 데이터를 특정함 */}
              <td>{fruit.id}</td> {/* 특정 fruit.id인 데이터의 key에 해당하는 value를 가져옴 */}
              <td>{fruit.name}</td>
              <td>{fruit.price.toLocaleString()}</td>
            </tr>
          )}
        </tbody>
      </Table >
    </>
  );
}

export default App;