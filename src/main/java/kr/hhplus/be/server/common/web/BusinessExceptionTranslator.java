package kr.hhplus.be.server.common.web;

import kr.hhplus.be.server.common.exception.BusinessError;
import kr.hhplus.be.server.common.exception.BusinessException;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.stereotype.Component;

@Component
class BusinessExceptionTranslator implements ExceptionTranslator<BusinessException> {

    @Override
    public BusinessException translate(Throwable t) {
        Throwable current = t;

        while (current != null) {
            if (current instanceof BusinessException e) {
                return e;
            }

            if (current instanceof ConcurrencyFailureException) {
                return new BusinessException(BusinessError.CONCURRENCY_FAILURE);
            }

            current = current.getCause();
        }

        return new BusinessException(BusinessError.INTERNAL_SERVER_ERROR);
    }

}
