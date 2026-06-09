export interface CartProduct {
    // 백엔드 엔티티에 있는 변수
    cartProductId: number;
    productId: number;
    quantity: number;

    // 백엔드 엔티티에 있지만 .ts에는 없는 변수
    // 프론트에서는 안쓰는 정보여서 넣지 않음
    // cartId: number


    // 백엔드 엔티티에 없는 변수
    image: string;
    name: string;
    price: number;
    checked: boolean; // 체크박스 상태 표시용
};