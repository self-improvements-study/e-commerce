# e-커머스 서비스 API 명세

## 1. 포인트
### 1-1 포인트 조회

[개요]  
설명: 사용자의 현재 보유 포인트를 조회

[요청 정보]

- HTTP Method: GET
- URL: /api/v1/points/{userId}
- Path Variable

Key | Type | Description
-- | -- | -- 
userId | Long| 사용자 ID

[응답 정보]

Response

```
{
  "code": 200,
  "message": "성공",
  "data": {
    "balance": 15000
  }
}
```
---
### 1-2 포인트 충전

[개요]  
설명: 사용자가 포인트를 충전

[요청 정보]

- HTTP Method: POST
- URL: /api/v1/points/charge
- Request Body

Key | Type | Description
-- | -- | --
userId | Long | 사용자 ID
amount | Long | 충전할 포인트

[응답 정보]

Response

```
{
  "code": 200,
  "message": "포인트 충전 성공",
  "data": {
    "balance": 20000
  }
}
```
---
### 1-3 포인트 내역 조회

[개요]  
설명: 사용자의 포인트 거래 내역을 조회

[요청 정보]

- HTTP Method: GET
- URL: /api/v1/points/histories/{userId}
- Path Variable

Key | Type | Description
-- | -- | --
userId | Long | 사용자 ID

[응답 정보]

Response

```
{
  "code": 200,
  "message": "성공",
  "data": [
    {
      "pointHistoryId": 1,
      "type": "CHARGE",
      "amount": 10000,
      "transactionDate": "2024-04-02T10:15:30"
    },
    {
      "pointHistoryId": 2,
      "type": "USE",
      "amount": 5000,
      "transactionDate": "2024-04-03T12:30:45"
    }
  ]
}
```
---
## 2. 상품
### 2-1 상품 조회

[개요]  
설명: 상품 정보를 조회

[요청 정보]

- HTTP Method: GET
- URL: /api/v1/products/{productId}
- Path Variable

Key | Type | Description
-- | -- | --
productId | Long | 상품 ID

[응답 정보]

Response

```
{
  "code": 200,
  "message": "성공",
  "data": {
    "productId": 1,
    "name": "운동화",
    "price": 99000,
    "options": [
      {
        "optionId": 101,
        "size": "260mm",
        "color": "화이트",
        "stock": 13
      },
      {
        "optionId": 102,
        "size": "270mm",
        "color": "블랙",
        "stock": 23
      }
    ]
  }
}
```
---
### 2-2 판매 우수 상품 조회

[개요]  
설명: 가장 많이 판매된 우수 상품을 조회

[요청 정보]

- HTTP Method: GET
- URL: /api/v1/products/top-sellers

[응답 정보]

Response

```
{
  "code": 200,
  "message": "성공",
  "data": [
    {
      "productId": 1,
      "name": "스마트폰"
    },
    {
      "productId": 2,
      "name": "태블릿"
    }
  ]
}
```
---
## 3. 주문
### 3-1 상품 주문

[개요]  
설명: 사용자가 상품을 주문

[요청 정보]

- HTTP Method: POST
- URL: /api/v1/orders/{userId}
- Path Variable

| Key    | Type | Description |
|--------|------|--------------|
| userId | Long | 사용자 ID    |

- Request Body

```json
{
  "orderItems": [
    {
      "optionId": 1,
      "quantity": 2,
      "userCouponId": 1001
    },
    {
      "optionId": 2,
      "quantity": 1,
      "userCouponId": null
    }
  ]
}
```

[응답 정보]

Response

```
{
  "code": 200,
  "message": "성공",
  "data": {
    "orderId": 12345,
    "totalAmount": 50000,
    "status": "PAYMENT_WAITING"
  }
}
```
---
### 3-2 주문 내역 조회

[개요]  
설명: 사용자의 주문 내역을 조회

[요청 정보]

- HTTP Method: GET
- URL: /api/v1/orders/{userId}
- Path Variable

Key | Type | Description
-- | -- | --
userId | Long | 사용자 ID

[응답 정보]

Response

```
{
  "code": 200,
  "message": "성공",
  "data": [
    {
      "orderId": 12345,
      "status": "SUCCESS",
      "totalAmount": 30000,
      "orderItems": [
        {
          "optionId": 101,
          "productName": "스니커즈",
          "size": "270mm",
          "color": "화이트",
          "quantity": 2,
          "price": 20000,
        },
        {
          "optionId": 205,
          "productName": "러닝화",
          "size": "280mm",
          "color": "블랙",
          "quantity": 1,
          "price": 10000,
        }
      ]
    }
  ]
}
```
---

### 3-2 주문 상세 내역 조회

[개요]  
설명: 사용자의 주문 상세 내역을 조회

[요청 정보]

- HTTP Method: GET
- URL: /api/v1/orders/{orderId}
- Path Variable

Key | Type | Description
-- | -- | --
orderId | Long |주문 ID

[응답 정보]

Response

```
{
  "code": 200,
  "message": "성공",
  "data": {
    "orderId": 12345,
    "status": "SUCCESS",
    "totalAmount": 30000,
    "orderItems": [
      {
        "optionId": 101,
        "productName": "스니커즈",
        "size": "270mm",
        "color": "화이트",
        "quantity": 2,
        "price": 20000
      },
      {
        "optionId": 205,
        "productName": "러닝화",
        "size": "280mm",
        "color": "블랙",
        "quantity": 1,
        "price": 10000
      }
    ],
    "payment": {
      "paymentId": 123,
      "orderId": 12345,
      "amount": 30000,
      "paymentDate": "2024-04-03T12:30:45"
    }
  }
}
```

---
## 4. 결제
### 4-1 주문 결제

[개요]  
설명: 사용자의 주문 결제 처리

[요청 정보]

- HTTP Method: POST
- URL: /api/v1/payments
- Request Body

Key | Type | Description
-- | -- | --
orderId | Long | 주문 ID

[응답 정보]

Response

```
{
  "code": 200,
  "message": "성공",
  "data": {
    "orderId": 1234,
    "paymentId": 98765,
    "amount": 30000,
    "paymentDate": "2024-04-03T12:30:45",
  }
}
```
---
## 5. 쿠폰
### 5-1 쿠폰 발급

[개요]  
설명: 사용자가 쿠폰을 발급받음

[요청 정보]

- HTTP Method: POST
- URL: /api/v1/coupons
- Request Body

Key | Type | Description
-- | -- | --
userId | Long | 사용자 ID
couponId | Long | 쿠폰 ID

[응답 정보]

Response

```
{
  "code": 200,
  "message": "성공",
  "data": {
    "userCouponId": 23,
    "couponId": 36,
    "couponName": "선착순 쿠폰",
    "discount": 3000,
    "startedDate": "2024-04-03T12:30:45",
    "endedDate": "2024-04-09T12:30:45"
  }
}
```
---
### 5-2 유저 쿠폰 조회

[개요]  
설명: 사용자의 보유 쿠폰을 조회

[요청 정보]

- HTTP Method: GET
- URL: /api/v1/coupons/{userId}
- Path Variable

Key | Type | Description
-- | -- | --
userId | Long | 사용자 ID

[응답 정보]

Response

```
{
  "code": 200,
  "message": "성공",
  "data": [
    {
      "userCouponId": 23,
      "couponId": 36,
      "couponName": "선착순 쿠폰",
      "discount": 3000,
      "startedDate": "2024-04-03T12:30:45",
      "endedDate": "2024-04-09T12:30:45"
    }
  ]
}
```
