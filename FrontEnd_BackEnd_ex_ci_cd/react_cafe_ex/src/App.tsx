import { useEffect, useState } from 'react';
import './App.css';

// 외부 컴포넌트 import하기
// import 컴포넌트이름 from '경로와 파일명';
import { useNavigate } from 'react-router-dom';
import AppRoutes from './routes/AppRoutes';
import type { User } from './types/User';
import MenuItems from './ui/MenuItems';

function App() {
  const appName = "IT Academy Coffee Shop";

  // 로그인 안한 상태 : User는 null
  // 로그인 한 상태 : User는 값이 있음
  const [user, setUser] = useState<User | null>(null);

  useEffect(() => { // 사이트가 처음 켜지거나 새로고침 했을때 setUser 관리
    // 로컬스토리지에 보관된 'user'정보를 가져옴
    const loginUser = localStorage.getItem('user');
    if (typeof loginUser === 'string') {
      // 로컬스토리지에 담긴 문자열인 'user'인 loginUser를 자바스크립트 객체{ } 형태로 형식으로 바꿈
      // key: value 형태
      const parsed = JSON.parse(loginUser);
      setUser(parsed);
    }
  }, []);

  const handleLoginSuccess = (userData: User) => { // LoginPage를 통해 로그인했을때 setUser 관리
    setUser(userData);
    localStorage.setItem('user', JSON.stringify(userData));
    console.log('로그인 성공');
  }

  const navigate = useNavigate();

  // 로그인한 사용자가 '로그 아웃' 버튼을 클릭했습니다.
  const handleLogout = (event: React.MouseEvent<HTMLElement>) => {
    event.preventDefault();
    setUser(null);
    localStorage.removeItem('user');
    localStorage.removeItem('accessToken');
    console.log('로그 아웃 성공');
    // 로그아웃시 이동할 페이지 설정
    navigate(`/member/login`);
  };

  return (
    <>
      {/* App.tsx의 appName변수를 MenuItems에 appName라는 변수명으로 프롭스로 주기 */}
      <MenuItems appName={appName} user={user} handleLogout={handleLogout} />

      {/* 분리된 라우터 정보 */}
      {/* handleLoginSuccess는 로그인 성공 후 사용자 상태를 갱신하는 함수 */}
      <AppRoutes user={user} handleLoginSuccess={handleLoginSuccess} />

      <footer className="bg-dark text-light text-center py-3 mt-5">
        <p>&copy; 2025 {appName}. All rights reserved.</p>
      </footer>
    </>
  );
}

export default App;