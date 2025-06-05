import http from 'k6/http';
import { check, sleep } from 'k6';
import { randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

// 선착순 쿠폰 발급 부하 테스트 시나리오
export const options = {
  scenarios: {
    concurrent_coupon_issuance: {
      executor: 'ramping-arrival-rate',  // 요청 비율을 점진적으로 증가
      startRate: 0,  // 시작 시 요청 비율
      timeUnit: '1s',
      preAllocatedVUs: 1,  // 초기 할당된 VUs
      maxVUs: 1700,  // 최대 VUs 수
      stages: [
        { target: 100 * 1.70, duration: '5s' },
        { target: 150 * 1.70, duration: '5s' },

        { target: 200 * 1.70, duration: '5s' },
        { target: 250 * 1.70, duration: '5s' },

        { target: 300 * 1.70, duration: '5s' },
        { target: 350 * 1.70, duration: '5s' },

        { target: 400 * 1.70, duration: '5s' },
        { target: 450 * 1.70, duration: '5s' },

        { target: 500 * 1.70, duration: '5s' },
        { target: 550 * 1.70, duration: '5s' },

        { target: 600 * 1.70, duration: '5s' },
        { target: 650 * 1.70, duration: '5s' },

        { target: 700 * 1.70, duration: '5s' },
        { target: 750 * 1.70, duration: '5s' },

        { target: 800 * 1.70, duration: '5s' },
        { target: 850 * 1.70, duration: '5s' },

        { target: 900 * 1.70, duration: '5s' },
        { target: 950 * 1.70, duration: '5s' },

        { target: 1000 * 1.70, duration: '5s' },
      ],
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<500'],  // 응답 시간 95%가 500ms 이하
    http_req_failed: ['rate<0.01'],    // 실패율 1% 미만
  },
};

export default function () {
  const url = 'http://localhost:8888/api/v1/coupons/enqueue';  // 쿠폰 발급 API URL
  const payload = JSON.stringify({
    userId: randomIntBetween(1, 10000),  // 랜덤한 userId
    couponId: 1,  // 발급할 쿠폰 ID (예: 1번 쿠폰)
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const res = http.post(url, payload, params);

  // 응답 상태 코드 체크
  check(res, {
    'status is 200': (r) => r.status === 200,
  });

  sleep(Math.random() * 2 + 1);
}