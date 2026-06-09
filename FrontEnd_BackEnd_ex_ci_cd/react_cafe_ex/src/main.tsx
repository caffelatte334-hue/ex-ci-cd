// 코드 작성
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import App from './App';

import 'bootstrap/dist/css/bootstrap.min.css'; // 부트 스트랩 적용

ReactDOM.createRoot(document.getElementById('root')!).render(
  // App.tsx에 속했는 파일들에 useNavigate, <Route>, <Link>등을 쓰기위해 설정하는 태그
  <BrowserRouter>
    <App />
  </BrowserRouter>
)