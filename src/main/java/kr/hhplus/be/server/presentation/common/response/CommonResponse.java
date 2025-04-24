package kr.hhplus.be.server.presentation.common.response;

import org.springframework.http.HttpStatus;

public record CommonResponse<T>(
        int code,
        String message,
        T data
) {

    public static <T> CommonResponse<T> success(T data) {
        return new CommonResponse<>(HttpStatus.OK.value(), "Success", data);
    }

    public static <T> CommonResponse<T> fail(HttpStatus status, String message) {
        return new CommonResponse<>(status.value(), message, null);
    }

}
