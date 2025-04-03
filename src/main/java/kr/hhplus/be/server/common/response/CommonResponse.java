package kr.hhplus.be.server.common.response;

public record CommonResponse<T>(
        int code,
        String message,
        T data
) {

    public static <T> CommonResponse<T> success(T data) {
        return new CommonResponse<>(200, "성공", data);
    }

    public static <T> CommonResponse<T> fail(int coed, String message) {
        return new CommonResponse<>(coed, message, null);
    }

}
