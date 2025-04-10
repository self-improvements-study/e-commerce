package kr.hhplus.be.server.presentation.common.response;

public record CommonResponse<T>(
        int code,
        String message,
        T data
) {

    public static <T> CommonResponse<T> success(T data) {
        return new CommonResponse<>(200, "Success", data);
    }

    public static <T> CommonResponse<T> fail(int code, String message) {
        return new CommonResponse<>(code, message, null);
    }

}
