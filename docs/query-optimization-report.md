# 1. 목적
- 이 보고서는 시스템에서 사용되는 주요 SQL 쿼리들의 성능을 분석하고, <br>
  이를 최적화하여 시스템의 응답 속도와 전체 성능을 향상시키기 위한 방안을 제시하는 데 목적이 있습니다.

# 2. 쿼리 성능 분석

## 2-1 쿠폰 조회
```sql
SELECT * FROM coupon WHERE id = ?;
```
### 개선 필요 여부 - 불필요한 개선
- 이유: id 컬럼은 보통 기본 키(primary key)로 설정되어 있고, 기본적으로 인덱스가 존재하므로 성능 문제는 없을 것입니다.


## 2-2 사용자가 보유한 쿠폰 목록 조회
```sql
    select
        uc1_0.user_coupon_id,
        c1_0.coupon_id,
        uc1_0.user_id,
        c1_0.coupon_name,
        c1_0.discount,
        c1_0.started_date,
        c1_0.ended_date,
        uc1_0.used 
    from
        user_coupon uc1_0 
    join
        coupon c1_0 
            on c1_0.coupon_id=uc1_0.coupon_id 
    where
        uc1_0.user_id=?
```
### 개선 필요 여부: 개선 필요
- 이유: userCoupon.userId, coupon.id, userCoupon.couponId에 인덱스가 없다면 성능 문제가 발생할 수 있습니다. <br>
  조인된 두 테이블을 처리하는데 시간이 걸릴 수 있습니다.
- 최적화 방향: userCoupon.userId, coupon.id, userCoupon.couponId에 인덱스를 추가

## 2-3 사용자가 보유한 쿠폰 목록 조회
```sql
SELECT * FROM point WHERE user_id = ?;
```
### 개선 필요 여부: 개선 필요
- 이유: userId에 대한 조회는 인덱스가 없다면 성능이 저하될 수 있습니다. 또한, 다른 테이블과 조인되는 경우, 해당 컬럼에 대한 인덱스도 필요할 수 있습니다.
- 최적화 방향: userId 컬럼에 인덱스를 추가.

## 2-4 포인트 내역 조회
```sql
SELECT * FROM point_history WHERE user_id = ?;
```
### 개선 필요 여부 - 불필요한 개선
- 이유: id 컬럼은 보통 기본 키(primary key)로 설정되어 있고, 기본적으로 인덱스가 존재하므로 성능 문제는 없을 것입니다.

## 2-5 상품 조회
```sql
SELECT * FROM product WHERE id = ?;
```
### 개선 필요 여부 - 불필요한 개선
- 이유: id 컬럼은 보통 기본 키(primary key)로 설정되어 있고, 기본적으로 인덱스가 존재하므로 성능 문제는 없을 것입니다.

## 2-6 인기 상품 조회
```sql
SELECT
    p1_0.product_id,
    p1_0.name,
    CAST(SUM(oi1_0.quantity) AS SIGNED) AS total_quantity
FROM
    product p1_0
JOIN
    product_option po1_0 
        ON p1_0.product_id = po1_0.product_id
JOIN
    order_item oi1_0 
        ON po1_0.product_option_id = oi1_0.option_id
JOIN
    orders o1_0 
        ON o1_0.order_id = oi1_0.order_id
WHERE
    o1_0.status = ?
    AND o1_0.order_date >= ?
GROUP BY
    p1_0.product_id,
    p1_0.name
ORDER BY
    CAST(SUM(oi1_0.quantity) AS SIGNED) DESC
LIMIT
    ?
```
### 개선 필요 여부 - 불필요한 개선
- 이유: 여러 테이블을 조인하고, 집계 함수(SUM)를 사용하며, GROUP BY와 ORDER BY도 포함된 복잡한 쿼리입니다. <br>
  조인된 테이블들에 적절한 인덱스가 없으면 성능이 크게 저하될 수 있습니다.

- 최적화 방향: 1. orderItem.optionId, productOption.productId, order.id 등 조인에 사용되는 컬럼에 인덱스를 추가<br>
  2. GROUP BY와 ORDER BY에 사용하는 컬럼에도 인덱스를 추가<br>
  3. 실행 계획을 통해 조인의 순서 및 인덱스 활용을 최적화<br>

## 2-7 주문 내역 조회
```sql
    select
        o1_0.order_id,
        o1_0.created_date,
        o1_0.last_modified_date,
        o1_0.order_date,
        o1_0.status,
        o1_0.total_price,
        o1_0.user_id 
    from
        orders o1_0 
    where
        o1_0.order_id=?
Hibernate: 
    select
        oi1_0.option_id,
        p1_0.name,
        po1_0.size,
        po1_0.color,
        oi1_0.quantity,
        oi1_0.user_coupon_id,
        oi1_0.original_price 
    from
        order_item oi1_0 
    join
        product_option po1_0 
            on oi1_0.option_id=po1_0.product_option_id 
    join
        product p1_0 
            on po1_0.product_id=p1_0.product_id 
    where
        oi1_0.order_id=?
```
### 개선 필요 여부: 개선 필요
- 이유: userId에 대한 조회는 인덱스가 없다면 성능이 저하될 수 있습니다. 또한, 다른 테이블과 조인되는 경우, 해당 컬럼에 대한 인덱스도 필요할 수 있습니다.
- 최적화 방향: userId 컬럼에 인덱스를 추가.<br>
  order_item.order_id에 인덱스를 추가하여 order_item 테이블과의 조인 성능을 개선할 수 있습니다.

## 2-8 주문 상세 내역 조회
```sql
SELECT * FROM orders WHERE order_id = ?;
```
### 개선 필요 여부 - 불필요한 개선
- 이유: id 컬럼은 보통 기본 키(primary key)로 설정되어 있고, 기본적으로 인덱스가 존재하므로 성능 문제는 없을 것입니다.
```sql
SELECT * FROM payment WHERE order_id = ?;
```
### 개선 필요 여부: 개선 필요
- 이유: order_id 컬럼에 인덱스가 없다면, 테이블이 커질수록 성능이 저하될 수 있습니다. order_id를 자주 사용하는 조회 조건이라면, 인덱스를 추가하는 것이 좋습니다.
- 최적화 방향: order_id에 대한 인덱스 추가: payment 테이블의 order_id 컬럼에 인덱스를 추가하여 조회 성능을 개선할 수 있습니다.

# 3. 인덱스 추가 및 성능 개선

- 이번 성능 개선 작업에서는 비슷한 쿼리들이 여러 번 사용되기 때문에, 실제로 중요한 부분만 테스트를 진행하였습니다. 이를 통해, 전체 쿼리 성능을 최적화할 수 있는 핵심 인덱스들만 선별하여 적용했습니다.

## 3-1 유저 쿠폰 목록 조회 성능 개선
### 테스트 환경
- 데이터량: 약 30,000건
- 기존 성능: 225ms
- 개선 후 성능: 185ms
- 성능 향상: 약 17.8% 성능 향상
### 성능 개선 사항
기존 쿼리에서는 user_id 컬럼에 인덱스가 없었기 때문에, user_coupon 테이블에서 user_id를 조건으로 조회할 때 성능이 저하되었습니다. <br>
이를 개선하기 위해, user_id 컬럼에 인덱스를 추가하여 조회 성능을 최적화했습니다.
```java
@Table(name = "user_coupon",
        indexes = {
                @Index(name = "idx_user_coupon_user_id", columnList = "user_id")
        }
)
```
## 3-2 주문 내역 조회 성능 개선
### 테스트 환경
- 기존 성능: 1s 340ms
- 개선 후 성능: 187ms
- 성능 향상 : 약 44.8% 성능 향상
### 성능 개선 사항
기존 쿼리에서는 order_id와 option_id 컬럼에 대한 인덱스가 없었기 때문에, order_item 테이블에서 이들 컬럼을 조건으로 조회할 때 성능이 저하되었습니다. <br>
이를 개선하기 위해 order_id와 option_id 컬럼에 각각 인덱스를 추가하여 조회 성능을 최적화했습니다.
```java
@Table(name = "user_coupon",
        indexes = {
                @Index(name = "idx_user_coupon_user_id", columnList = "user_id")
        }
)
```

## 3-2 인기 상품 조회 성능 개선
### 테스트 환경
- 주문 테이블: 약 100만 건
- 주문 아이템 테이블: 약 300만 건
- 날짜 조건: 30일 이전의 주문 데이터
- 기존 성능: 11s
- 개선 후 성능: 2s 635ms
- 성능 향상: 약 76% 성능 향상

### 성능 개선 사항
기존 쿼리에서는 order_item 테이블에서 order_id와 option_id 컬럼을 조건으로 조회할 때 인덱스가 없어 성능이 저하되었습니다. 이를 개선하기 위해 각 컬럼에 인덱스를 추가하여 조회 성능을 최적화했습니다.

인덱스 추가 내용 - order_id와 option_id 컬럼에 인덱스를 추가하여 조회 성능을 개선했습니다.
```java
@Table(name = "order_item",
        indexes = {
                @Index(name = "idx_order_item_order_id", columnList = "order_id"),
                @Index(name = "idx_order_item_option_id", columnList = "option_id"),
        }
)
```

인덱스 추가 내용 - order_id와 option_id 컬럼에 인덱스를 추가하여 조회 성능을 개선했습니다.
```java
@Table(name = "order_item",
        indexes = {
                @Index(name = "idx_order_item_order_id", columnList = "order_id"),
                @Index(name = "idx_order_item_option_id", columnList = "option_id"),
        }
)
```

인덱스 추가 내용 - orderDate와 status 컬럼에 인덱스를 추가하여 주문 날짜와 상태를 기준으로 쿼리 성능을 개선했습니다.
```java
@Table(name = "orders",
indexes = {
        @Index(name = "idx_orders_orderDate", columnList = "orderDate"),
        @Index(name = "idx_orders_status", columnList = "status"),
})
```
인덱스 추가 내용 - product_id 컬럼에 인덱스를 추가하여 상품 옵션을 조회할 때 성능을 최적화했습니다.
```java
@Table(name = "product_option",
        indexes = {
                @Index(name = "idx_product_option_product_id", columnList = "product_id")
        }
)
```

# 4. 결론
- 이번 성능 개선 작업을 통해, 시스템에서 주요 쿼리들의 성능을 크게 향상시킬 수 있었습니다. 쿼리 성능 분석과 인덱스 최적화를 통해, 데이터 조회 성능이 개선되었으며, 특히 대규모 데이터에서의 처리 속도가 크게 향상되었습니다.
- 현재 인기 상품 조회 쿼리에서 성능 향상이 미흡한 이유는 도메인 특성상 FROM 절에서 product 테이블을 기준으로 쿼리를 시작하고 있기 때문입니다.<br>
  이로 인해, 조인되는 다른 테이블들에 비해 성능이 다소 저하될 수 있습니다. 또한, 인덱스의 한계와 기술적 확장성에 대한 부분도 고려해야 할 사항입니다.

# 5. 추후 계획
### 인덱스의 한계
인덱스를 추가하면 성능이 개선될 수 있지만, 인덱스의 수가 많아지면 인덱스의 유지 관리가 복잡해지고, 쓰기 성능에 영향을 줄 수 있습니다. 특히 대량의 데이터 삽입 및 업데이트가 빈번한 시스템에서는 인덱스 갱신 비용이 크게 증가할 수 있습니다.
### 레디스를 이용한 캐싱 전략
인기 상품 조회는 보통 실시간 계산이 아닌 집계 데이터를 반환합니다. 이러한 데이터는 일정 주기마다 변하지 않기 때문에, 레디스에 캐시해두고 주기적으로 갱신하는 방식으로 성능을 개선할 수 있습니다. <br>
캐싱 전략 예시:
- 인기 상품 목록을 일정 시간(예: 1분, 5분)마다 캐시에 저장합니다.
- 클라이언트가 인기 상품을 조회할 때는 레디스에서 캐시된 데이터를 조회하고, 캐시가 만료되었거나 데이터가 없으면 DB에서 조회하여 다시 레디스에 저장합니다.

### 레디스 활용에 대한 장점
- 빠른 응답 시간: 레디스는 인메모리 데이터베이스이기 때문에, 디스크 기반의 DB보다 훨씬 빠른 읽기 성능을 제공합니다.
- 부하 분산: 데이터베이스의 부하를 분산시켜, 시스템의 전체적인 성능을 향상시킬 수 있습니다.



