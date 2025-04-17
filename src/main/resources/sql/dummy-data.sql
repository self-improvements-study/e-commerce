SET SESSION cte_max_recursion_depth = 10000000;

-- user --------------------------------------------------------------------------

INSERT INTO user (user_id, name,created_date, last_modified_date)
WITH RECURSIVE cte3 (n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM cte3 WHERE n <= 100000
)
SELECT
    n AS id,
    CONCAT('이름', LPAD(n, 6, '0')) AS name,
    NOW(),
    NOW()
FROM cte3;

-- point --------------------------------------------------------------------------

INSERT INTO point (point_id, user_id, balance, created_date, last_modified_date)
WITH RECURSIVE cte1 (n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM cte1 WHERE n < 100000
)
SELECT
    n AS point_id,
    n AS user_id,
    CAST(10000 + (RAND() * 50000) AS UNSIGNED),
    FROM_UNIXTIME(UNIX_TIMESTAMP(NOW()) - FLOOR(RAND() * 31536000)),
    FROM_UNIXTIME(UNIX_TIMESTAMP(NOW()) - FLOOR(RAND() * 31536000))
FROM cte1;

-- pointHistory ---------------------------------------------------------------------

INSERT INTO point_history (point_history_id, user_id, amount, type, created_date, last_modified_date)
WITH RECURSIVE cte2 (n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM cte2 WHERE n < 100000
)
SELECT
    n AS point_history_id,
    n AS user_id,
    CAST(1000 + (RAND() * 10000) AS UNSIGNED),
    IF(RAND() < 0.5, 'CHARGE', 'PAYMENT'),
    FROM_UNIXTIME(UNIX_TIMESTAMP(NOW()) - FLOOR(RAND() * 31536000)),
    FROM_UNIXTIME(UNIX_TIMESTAMP(NOW()) - FLOOR(RAND() * 31536000))
FROM cte2;

-- coupon ---------------------------------------------------------------------

INSERT INTO coupon (
    coupon_id, coupon_name, discount, quantity,
    started_date, ended_date, created_date, last_modified_date
)
WITH RECURSIVE cte_coupon (n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM cte_coupon WHERE n < 100000
)
SELECT
    n AS coupon_id,
    CONCAT('쿠폰', LPAD(n, 5, '0')) AS coupon_name,
    CAST(1000 + (RAND() * 4000) AS UNSIGNED),
    CAST(1 + (RAND() * 10000) AS UNSIGNED),
    NOW() - INTERVAL 1 DAY,
    NOW() + INTERVAL 30 DAY,
    NOW(),
    NOW()
FROM cte_coupon;

-- userCoupon ---------------------------------------------------------------------

INSERT INTO user_coupon (
    user_coupon_id, user_id, coupon_id, used,
    created_date, last_modified_date
)
WITH RECURSIVE cte_user_coupon (n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM cte_user_coupon WHERE n < 100000
)
SELECT
    n AS user_coupon_id,
    FLOOR(1 + RAND() * 100000) AS user_id,     -- 유저: 1~100000
    FLOOR(1 + RAND() * 100000) AS coupon_id,   -- 쿠폰: 1~100000
    IF(RAND() < 0.3, 'Y', 'N'),              -- 30% 사용 처리
    NOW(),
    NOW()
FROM cte_user_coupon;

-- product ---------------------------------------------------------------------

INSERT INTO product (name, price, created_date, last_modified_date)
WITH RECURSIVE cte_product (n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM cte_product WHERE n <= 10000
)
SELECT
    CONCAT('제품', n),
    CAST(1000 + (RAND() * 9000) AS UNSIGNED),  -- 가격 랜덤
    NOW(),
    NOW()
FROM cte_product;

-- productOption ---------------------------------------------------------------------

INSERT INTO product_option (product_option_id, product_id, color, size, created_date, last_modified_date)
WITH RECURSIVE cte_option (n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM cte_option WHERE n <= 100000
)
SELECT
    n,
    FLOOR(n / 10) + 1 AS product_id,
    CONCAT('컬러', (n % 10) + 1),
    CONCAT('사이즈', (n % 5) + 1),
    NOW(),
    NOW()
FROM cte_option;

-- stock ---------------------------------------------------------------------

INSERT INTO stock (stock.stock_id, product_option_id, quantity, created_date, last_modified_date)
WITH RECURSIVE cte_stock (n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM cte_stock WHERE n <= 100000
)
SELECT
    n ,
    n AS product_option_id,
    FLOOR(1 + RAND() * 100),  -- 재고: 1~100
    NOW(),
    NOW()
FROM cte_stock;

-- order ---------------------------------------------------------------------

INSERT INTO orders (user_id, order_date, status, total_price, created_date, last_modified_date)
WITH RECURSIVE cte_orders (n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM cte_orders WHERE n <= 1000000
)
SELECT
    FLOOR(1 + RAND() * 100000), -- user_id: 1 ~ 100000
    NOW() - INTERVAL FLOOR(RAND() * 30) DAY,
    FLOOR(RAND() * 3),
    FLOOR(5000 + (RAND() * 50000)),
    NOW(),
    NOW()
FROM cte_orders;

-- orderItem ---------------------------------------------------------------------

INSERT INTO order_item (order_item_id, order_id, option_id, quantity, original_price, user_coupon_id, created_date, last_modified_date)
WITH RECURSIVE cte_items (n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM cte_items WHERE n <= 3000000
)
SELECT
    n,
    FLOOR(n / 3) + 1,
    FLOOR(1 + RAND() * 100000) AS option_id,           -- option_id (1~10000) // 1~100000, unique each order_id
    FLOOR(1 + RAND() * 5),             -- quantity
    FLOOR(1000 + (RAND() * 5000)),     -- original_price
    IF(RAND() < 0.3, FLOOR(1 + RAND() * 100000), NULL),  -- 30% 확률로 user_coupon_id, 나머지는 NULL
    NOW(),
    NOW()
FROM cte_items;
