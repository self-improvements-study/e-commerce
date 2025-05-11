package kr.hhplus.be.server.common.redisson;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Aspect
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RedissonLockAspect {

    private final RedissonClient redissonClient;

    private final SPelEvaluator spelEvaluator;

    @Around("@annotation(DistributedLock)")
    public Object lock(final ProceedingJoinPoint joinPoint) throws Throwable {

        // 1. 메서드 정보 가져오기
        Method method = getMethod(joinPoint);

        // 2. 메서드에서 @DistributedLock 애노테이션 가져오기
        DistributedLock annotation = AnnotationUtils.findAnnotation(method, DistributedLock.class);

        // 3. 메서드 파라미터와 값을 리스트로 매핑
        Object[] args = joinPoint.getArgs();

        List<Pair<Parameter, Object>> pairs = getParameterPairs(args, method);

        // 4. keyExpression 추출
        String keyExpression = annotation.keyExpression();

        if (!StringUtils.hasText(keyExpression)) {
            throw new AssertionError();
        }

        // 5. keyExpression 에서 사용할 파라미터 이름 추출
        String keyParameterName = getKeyParameterName(keyExpression);

        // 6. 파라미터 값과 keyExpression 에서 추출한 파라미터 이름을 매칭
        Pair<Parameter, Object> pair = pairs.stream()
                .filter(it -> it.getFirst().getName().equals(keyParameterName))
                .findFirst()
                .orElseThrow();

        // 7. SpEL을 사용하여 keyExpression 을 평가하여 동적으로 락 키를 계산
        String pairFirst = pair.getFirst().getName();
        Object pairSecond = pair.getSecond();
        Object idObject = spelEvaluator.evaluateExpression(pairFirst, pairSecond, keyExpression);

        // 8. 평가된 결과가 Object[]나 Collection 이면 리스트로 변환
        List<Object> ids = new ArrayList<>();
        if (idObject instanceof Object[] array) {
            ids.addAll(List.of(array));
        } else if (idObject instanceof Collection<?> collection) {
            ids.addAll(collection);
        } else {
            ids.add(idObject);
        }

        // 9. 락 키들을 기반으로 Redisson 클라이언트를 이용해 RLock 객체 생성
        String topic = annotation.topic();
        RLock[] locks = ids.stream()
                .map(id -> "%s:%s".formatted(topic, id))
                .map(redissonClient::getLock)
                .toArray(RLock[]::new);

        // 10. RLock 배열을 멀티락으로 묶어서 동시에 여러 락을 관리
        RLock multiLock = redissonClient.getMultiLock(locks);

        boolean available = false;
        try {
            long waitTime = annotation.waitTime();
            long leaseTime = annotation.leaseTime();
            TimeUnit unit = annotation.unit();

            // 11. 락을 시도
            available = multiLock.tryLock(waitTime, leaseTime, unit);

            if (!available) {
                throw new IllegalStateException("락 획득 실패 - ids: " + ids);
            }

            // 12. 락이 획득되면 실제 메서드를 실행
            return joinPoint.proceed();
        } finally {
            // 13. 락을 사용한 후, 반드시 락 해제
            if (available) {
                multiLock.unlock();
            }
        }
    }

    /**
     * 주어진 keyExpression 에서 락 키로 사용할 파라미터 이름을 추출하는 메서드
     * @param keyExpression SpEL 표현식에서 사용될 파라미터
     * @return 파라미터 이름
     */
    private String getKeyParameterName(String keyExpression) {
        String keyParameterName;
        Pattern pattern = Pattern.compile("#(\\w+)");
        Matcher matcher = pattern.matcher(keyExpression);

        if (matcher.find()) {
            keyParameterName = matcher.group(1);
        } else {
            throw new AssertionError();
        }

        if (!StringUtils.hasText(keyParameterName)) {
            throw new AssertionError();
        }

        return keyParameterName;
    }

    /**
     * AOP JoinPoint 에서 메서드를 가져오는 메서드
     * @param joinPoint ProceedingJoinPoint
     * @return 실행된 메서드
     */
    private Method getMethod(ProceedingJoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        if (!(signature instanceof MethodSignature ms)) {
            throw new AssertionError();
        }
        return ms.getMethod();
    }

    /**
     * AOP JoinPoint에서 메서드 파라미터와 값을 가져와서 Pair 리스트로 반환하는 메서드
     * @param args Object[]
     * @param method Method
     * @return 메서드 파라미터와 값들의 Pair 리스트
     */
    private List<Pair<Parameter, Object>> getParameterPairs(Object[] args, Method method) {
        Parameter[] parameters = method.getParameters();

        List<Pair<Parameter, Object>> pairs = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            pairs.add(Pair.of(parameters[i], args[i]));
        }
        return pairs;
    }

}
