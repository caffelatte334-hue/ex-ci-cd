// 리액트에서 사용하는 상단 영역의 메뉴 정보를 저장해 놓은 파일
import { NavDropdown, Navbar, Container, Nav } from "react-bootstrap";

import { useNavigate } from "react-router-dom";
import type { User } from "../types/User";

/*
- 부모에서 1개의 props를 자식에게 주면
자식은 2개의 행동을 해야 함
-> 1) 받은 프롭스의 type 작성 / 2) 매개변수에 해당 프롭스 추가 및 type 지정
*/

// 1) 받은 프롭스의 type 작성
// App.tsx에서 받은 프롭스를 해당 컴포넌트에서 다시 타입 정의
type MenuItemsProps = {
   appName: string;
   user: User | null; // 이 데이터는 null일 수도 있습니다.
   handleLogout: (event: React.MouseEvent<HTMLElement>) => void; // App.tsx에서 받아옴
};

// 2) 매개변수에 해당 프롭스 추가 및 type 지정
// 원래 프롭스에서 프롭을 꺼낼때는 {}을 사용함
// 매개변수에 넣어서 해당 함수 안에는 원래 변수처럼 {}없이 사용하게 하기
// 해당 매개변수의 타입이 해당 컴포넌트에서 정의한 타입을 이용해서 다시 정의 함
function App({ appName, user, handleLogout }: MenuItemsProps) {
   console.log('appName 프롭스 : ' + appName);
   const navigate = useNavigate();

   // user 프롭스를 사용하여 상단에 보이는 풀다운 메뉴를 적절히 분기 처리합니다.
   // App.tsx에서 받은 프롭스인 user에 들어있는 role을 이용 (LoginPage.tsx에서 얻은 데이터)
   // 원래 switch-case문은 break가 있을때까지 쭉 진행하지만 여기서는 return이 이 역할을 대신함
   const renderMenu = () => {
      switch (user?.role) {
         case 'ADMIN':
            return (
               <>
                  <Nav.Link onClick={() => navigate(`/product/insert`)}>상품 등록</Nav.Link>
                  {/* 관리자는 모든 사람의 주문 내역 확인 */}
                  <Nav.Link onClick={() => navigate(`/order/list`)}>주문 내역</Nav.Link>
                  <Nav.Link onClick={handleLogout}>로그 아웃</Nav.Link>
               </>
            );
         case 'USER':
            return (
               <>
                  <Nav.Link onClick={() => navigate(`/cart/list`)}>장바구니</Nav.Link>
                  <Nav.Link onClick={() => navigate(`/order/list`)}>주문 내역</Nav.Link>
                  <Nav.Link onClick={handleLogout}>로그 아웃</Nav.Link>
               </>
            );
         default:
            return (
               <>
                  <Nav.Link onClick={() => navigate(`/member/login`)}>로그인</Nav.Link>
                  <Nav.Link onClick={() => navigate(`/member/signup`)}>회원 가입</Nav.Link>
               </>
            );
      }
   };

   return (
      <Navbar bg="dark" variant="dark" expand="lg">
         <Container>
            {/* 매개변수로 받은 프롭 사용하기 */}
            <Navbar.Brand href='/'>{appName}</Navbar.Brand>
            <Nav className="me-auto">
               {/* 사용자 이름 표시 */}
               {user && (
                  <Nav.Item className="text-white fw-bold fs-5 me-3 d-flex align-items-center">
                     {user.name}님
                  </Nav.Item>
               )}

               {/* 하이퍼링크 : Nav.Link는 다른 페이지로 이동할 때 사용됩니다.  */}
               <Nav.Link onClick={() => navigate(`/product/list`)}>상품 보기</Nav.Link>

               {renderMenu()}

               <NavDropdown title={`기본 연습`}>
                  <NavDropdown.Item onClick={() => navigate(`/fruit`)}>과일 1개</NavDropdown.Item>
                  <NavDropdown.Item onClick={() => navigate(`/fruit/list`)}>과일 목록</NavDropdown.Item>
               </NavDropdown>
            </Nav>
         </Container>
      </Navbar >
   );
}

export default App;