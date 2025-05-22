package kr.hhplus.be.server.presentation.product;

import kr.hhplus.be.server.application.order.OrderCriteria;
import kr.hhplus.be.server.common.event.EventFlowManager;
import kr.hhplus.be.server.common.event.EventFlowState;
import kr.hhplus.be.server.common.event.OrderEventFlow;
import kr.hhplus.be.server.domain.coupon.CouponInfo;
import kr.hhplus.be.server.domain.order.OrderCommand;
import kr.hhplus.be.server.domain.order.OrderCreatedEvent;
import kr.hhplus.be.server.domain.order.OrderEvent;
import kr.hhplus.be.server.domain.payment.PaymentCommand;
import kr.hhplus.be.server.domain.payment.PaymentEvent;
import kr.hhplus.be.server.domain.product.ProductCommand;
import kr.hhplus.be.server.domain.product.ProductInfo;
import kr.hhplus.be.server.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductEventListener {

    private final ProductService productService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void decreaseStock(OrderEvent.CreateOrder event) {

        List<ProductCommand.OptionStock> optionStocks = event.getOrderItem().stream()
                .map(item -> ProductCommand.OptionStock.of(item.getOptionId(), item.getQuantity()))
                .collect(Collectors.toList());

        productService.decreaseStockQuantity(ProductCommand.DecreaseStock.of(optionStocks));

    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void createStoreProductSignal(PaymentEvent.CreatePayment event) {

        List<ProductCommand.ProductSignal> productSignalList = event.getProductSignalList().stream()
                .map(item -> ProductCommand.ProductSignal
                        .of(item.getProductId(), item.getDate(), item.getName(), item.getQuantity()))
                .toList();

        productService.storeProductSignal(productSignalList);

    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void increaseStock(PaymentEvent.CreatePayment event) {

        List<ProductCommand.OptionStock> optionStocks = event.getOptionStockList().stream()
                .map(v -> ProductCommand.OptionStock.of(v.getOptionId(), v.getQuantity()))
                .toList();

        productService.increaseStockQuantity(ProductCommand.IncreaseStock.of(optionStocks));
    }

}
