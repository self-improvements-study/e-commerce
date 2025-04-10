package kr.hhplus.be.server.common.exception;

import lombok.Getter;

import java.util.Objects;

@Getter
public class BusinessException extends RuntimeException{

    private final BusinessError businessError;
    public BusinessException(BusinessError businessError) {
        this(businessError, businessError.getMessage());
    }

    public BusinessException(BusinessError businessError, String message) {
        super(message);
        this.businessError = Objects.requireNonNull(businessError, "businessError cannot be null");
    }

}
