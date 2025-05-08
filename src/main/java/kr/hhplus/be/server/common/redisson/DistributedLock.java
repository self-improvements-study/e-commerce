package kr.hhplus.be.server.common.redisson;

import org.intellij.lang.annotations.Language;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    String topic();

    @Language("SpEL")
    String keyExpression();

    long waitTime();

    long leaseTime();

    TimeUnit unit() default TimeUnit.SECONDS;

}
