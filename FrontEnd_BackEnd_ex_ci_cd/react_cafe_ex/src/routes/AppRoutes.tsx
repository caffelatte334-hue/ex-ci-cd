import { Route, Routes } from "react-router-dom";

import FruitOne from './../pages/FruitOne';
import FruitList from './../pages/FruitList';

import HomePage from './../pages/HomePage';

import SignupPage from './../pages/SignupPage';
import LoginPage from './../pages/LoginPage';
import ProductList from './../pages/ProductList';
import ProductInsertForm from './../pages/ProductInsertForm';
import ProductUpdateForm from './../pages/ProductUpdateForm';
import ProductDetail from './../pages/ProductDetail';
import CartList from './../pages/CartList';

import type { User } from "../types/User";

interface AppProps { // App.tsx에서 온 프롭스 - LoginPage.tsx에 전달만 함
  user: User | null; // 로그인하면 App.tsx의 setUser로 의미있는 데이터가 되어 프롭스로 받아짐 (로그인안하면 null)
  handleLoginSuccess: (userData: User) => void;
}

function App({ user, handleLoginSuccess }: AppProps) {
  return (
    <Routes>
      {/* url에 /fruit 요청이 오면 컴포넌트로 들어가진 FruitOne 파일을 사용함 */}
      <Route path="/fruit" element={<FruitOne />} />
      {/* url에 /fruit/list 요청이 오면 컴포넌트로 들어가진 FruitList 파일을 사용함 */}
      <Route path="/fruit/list" element={<FruitList />} />

      <Route path="/" element={<HomePage />} />

      <Route path='/member/signup' element={<SignupPage />} />
      <Route path='/member/login' element={<LoginPage onLogin={handleLoginSuccess} />} />
      <Route path='/product/list' element={<ProductList user={user} />} />
      <Route path='/product/insert' element={<ProductInsertForm user={user} />} />
      {/* ProductController의 @DeleteMapping("/delete/{id}")처럼 {id}를 경로 변수, 가변 매개 변수라고
      하는 것처럼 여기서 :기호도 id가 가변적인 변수라는 것을 의미하는 기호임
      실제로 주소창에는 :이 기호가 사라지고 순수한 id값만 오게됨 */}
      <Route path='/product/update/:id' element={<ProductUpdateForm user={user} />} />
      <Route path='/product/detail/:id' element={<ProductDetail user={user} />} />
      <Route path='/cart/list' element={<CartList user={user} />} />

    </Routes>
  );
}

export default App;