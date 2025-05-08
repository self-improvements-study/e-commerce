package kr.hhplus.be.server.test.testdouble;

import kr.hhplus.be.server.common.redisson.DistributedLock;
import lombok.Builder;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FakeDistributedLockService {

    @DistributedLock(
            topic = "stock",
            keyExpression = "#dummy.toOptionIds()",
            waitTime = 5,
            leaseTime = 3
    )
    public void fakeDistributedLock(Dummy dummy, Runnable runnable) {
        runnable.run();
    }

    @Getter
    @Builder
    public static class Dummy {
        private List<Item> items;

        public List<Long> toOptionIds() {
            return this.items.stream().map(Item::getOptionId).sorted().toList();
        }
    }

    @Getter
    @Builder
    public static class Item {
        private long optionId;
    }
}
