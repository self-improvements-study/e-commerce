import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  scenarios: {
    spike: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '5s', target: 10 },
        { duration: '5s', target: 30 },
        { duration: '5s', target: 50 },
        { duration: '5s', target: 30 },
        { duration: '5s', target: 10 },
        { duration: '5s', target: 0 }
      ],
    },
  }
};

export default function () {
  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };
  const host = 'http://localhost:8080';

  const response = http.get(`${host}/api/v1/products/top-sellers`, params);

  check(response, {
    'Status is 200': (r) => r.status === 200,
  });
}
