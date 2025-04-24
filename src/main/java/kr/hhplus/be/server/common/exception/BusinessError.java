package kr.hhplus.be.server.common.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@ToString
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum BusinessError {

    // User ---------------------------------------------------------------------------------------------------

    NO_REGISTERED_USER("U001", HttpStatus.BAD_REQUEST, "존재하지 않는 유저입니다."),

    // Point --------------------------------------------------------------------------------------------------

    POINT_AMOUNT_INCREASE_TOO_SMALL("P001", HttpStatus.BAD_REQUEST, "충전 금액은 1원 이상이어야 합니다."),
    POINT_AMOUNT_DECREASE_TOO_SMALL("P002", HttpStatus.BAD_REQUEST, "사용 금액은 1원 이상이어야 합니다."),
    POINT_AMOUNT_INCREASE_EXCEEDS_LIMIT("P003", HttpStatus.BAD_REQUEST, "충전 금액이 허용된 최대치를 초과했습니다."),
    POINT_AMOUNT_DECREASE_EXCEEDS_LIMIT("P004", HttpStatus.BAD_REQUEST, "사용 금액이 허용된 최대치를 초과했습니다."),
    POINT_BALANCE_EXCEEDED("P005", HttpStatus.BAD_REQUEST, "사용 금액이 잔액을 초과했습니다."),

    // Product --------------------------------------------------------------------------------------------------

    PRODUCT_NOT_FOUND("PR001", HttpStatus.NOT_FOUND, "해당 상품이 존재하지 않습니다."),
    PRODUCT_OPTION_NOT_FOUND("PR002", HttpStatus.NOT_FOUND, "해당 상품 옵션이 존재하지 않습니다."),
    PRODUCT_STOCK_NOT_FOUND("PR003", HttpStatus.NOT_FOUND, "해당 상품 옵션의 재고 정보가 존재하지 않습니다."),

    // Stock --------------------------------------------------------------------------------------------------

    STOCK_NOT_FOUND("ST001", HttpStatus.NOT_FOUND, "해당 재고가 존재하지 않습니다."),
    STOCK_QUANTITY_INVALID("ST002", HttpStatus.BAD_REQUEST, "재고 수량은 1개 이상이어야 합니다."),
    STOCK_QUANTITY_EXCEEDED("ST003", HttpStatus.BAD_REQUEST, "요청 수량이 현재 재고보다 많습니다."),
    STOCK_OPERATION_EMPTY("ST004", HttpStatus.BAD_REQUEST, "재고 변경 요청이 비어있습니다."),

    // Coupon --------------------------------------------------------------------------------------------------

    COUPON_NOT_FOUND("C001", HttpStatus.NOT_FOUND, "해당 쿠폰이 존재하지 않습니다."),
    USER_COUPON_NOT_FOUND("C002", HttpStatus.NOT_FOUND, "해당 유저의 쿠폰이 존재하지 않습니다."),
    COUPON_ALREADY_USED("C003", HttpStatus.BAD_REQUEST, "이미 사용된 쿠폰입니다."),
    COUPON_EXPIRED("C004", HttpStatus.BAD_REQUEST, "만료된 쿠폰입니다."),
    COUPON_ALREADY_ISSUED("C005", HttpStatus.CONFLICT, "이미 발급된 쿠폰입니다."),
    COUPON_ISSUE_LIMIT_EXCEEDED("C006", HttpStatus.BAD_REQUEST, "쿠폰 발급 한도를 초과했습니다."),
    COUPON_USAGE_NOT_ALLOWED("C007", HttpStatus.FORBIDDEN, "해당 쿠폰은 현재 사용할 수 없습니다."),
    INVALID_COUPON_QUANTITY("C008", HttpStatus.BAD_REQUEST, "차감 수량은 0보다 커야 합니다."),
    INVALID_DISCOUNT_PRICE("O009", HttpStatus.BAD_REQUEST, "할인 금액은 상품 금액보다 작아야 합니다."),
    COUPON_NOT_USED("O010", HttpStatus.BAD_REQUEST, "할인 금액은 상품 금액보다 작아야 합니다."),

    // Order --------------------------------------------------------------------------------------------------

    ORDER_ITEMS_EMPTY("O001", HttpStatus.BAD_REQUEST, "주문 항목이 비어있습니다."),
    DUPLICATE_OPTION_ID("O002", HttpStatus.BAD_REQUEST, "주문 항목에 중복된 옵션이 존재합니다."),
    DUPLICATE_USER_COUPON_ID("O003", HttpStatus.BAD_REQUEST, "사용자 쿠폰이 중복되어 있습니다."),
    INVALID_ITEM_QUANTITY("O004", HttpStatus.BAD_REQUEST, "주문 수량은 1개 이상이어야 합니다."),
    INVALID_ORDER_STATUS("O005", HttpStatus.BAD_REQUEST, "주문 상태가 유효하지 않습니다."),
    ORDER_NOT_FOUND("O006", HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),

    // Payment --------------------------------------------------------------------------------------------------

    PAYMENT_FAILED("P001", HttpStatus.INTERNAL_SERVER_ERROR, "결제에 실패하였습니다."),
    PAYMENT_NOT_FOUND("P002", HttpStatus.NOT_FOUND, "해당 결제내역이 존재하지 않습니다."),
    PAYMENT_AMOUNT_DECREASE_TOO_SMALL("P003", HttpStatus.NOT_FOUND, "결제 금액은 0원 이상이어야 합니다."),

    // System ------------------------------------------------------------------------------------------------

    INTERNAL_SERVER_ERROR("C001", HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),
    INVALID_REQUEST("C002", HttpStatus.BAD_REQUEST, "잘못된 요청입니다.")
    ;

    private final String code;
    private final HttpStatus status;
    private final String message;

    BusinessError(String code, HttpStatus status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }
}
