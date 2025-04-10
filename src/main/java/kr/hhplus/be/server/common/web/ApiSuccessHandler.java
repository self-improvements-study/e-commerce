package kr.hhplus.be.server.common.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.presentation.common.response.CommonResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.math.BigDecimal;
import java.math.BigInteger;

@ControllerAdvice
public class ApiSuccessHandler implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        // 모든 API 응답을 처리하도록 true 반환
        Class<?> type = returnType.getParameterType();

        // 이미 ResponseEntity 형태이면 변경하지 않고 그대로 반환
        if (ResponseEntity.class.isAssignableFrom(type)) {
            return false;
        }

        if (CommonResponse.class.isAssignableFrom(type)) {
            return false;
        }

        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class selectedConverterType,
                                  org.springframework.http.server.ServerHttpRequest request,
                                  org.springframework.http.server.ServerHttpResponse response) {
        CommonResponse responseDto = CommonResponse.success(body);

        // 만약 컨트롤러가 String을 반환하면 JSON 문자열로 변환 필요
        if (isNonPojo(body)) {
            try {
                HttpHeaders headers = response.getHeaders();
                headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                return new ObjectMapper().writeValueAsString(responseDto);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("JSON 변환 오류", e);
            }
        }

        return responseDto;
    }

    private static boolean isNonPojo(Object o) {
        return o instanceof Boolean
                || o instanceof Byte
                || o instanceof Short
                || o instanceof Integer
                || o instanceof Long
                || o instanceof Float
                || o instanceof Double
                || o instanceof String
                || o instanceof BigInteger
                || o instanceof BigDecimal;
    }

}
