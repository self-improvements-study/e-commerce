package kr.hhplus.be.server.test.util;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BuilderArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FailoverIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import lombok.Getter;

import java.util.List;
import java.util.Random;

public final class RandomGenerator {

    private static final Random RANDOM = new Random();

    @Getter
    private static final FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
            .objectIntrospector(new FailoverIntrospector(List.of(
                    FieldReflectionArbitraryIntrospector.INSTANCE,
                    BuilderArbitraryIntrospector.INSTANCE
            )))
            .defaultNotNull(true)
            .enableLoggingFail(false)
            .build();

    public static long nextLong(long min, long max) {
        return RANDOM.nextLong(min, max);
    }

    public static long nextPositiveLong(long max) {
        return nextLong(1, max);
    }

    public static long nextPositiveOrZeroLong(long max) {
        return nextLong(0, max);
    }

    public static long nextNegativeLong(long min) {
        return nextLong(min, 0);
    }

    public static long nextNegativeOrZeroLong(long min) {
        return nextLong(min, 1);
    }

}
