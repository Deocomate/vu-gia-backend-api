package vn.springboot.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.common.exception.ErrorCode;

import java.time.Instant;

/**
 * Standard envelope for every HTTP response (both success and error).
 * Shape is always: {@code {code, message, data, timestamp}}.
 *
 * @param <T> type of the payload
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.ALWAYS)
public class ApiResponse<T> {

    /** Business status code. {@code 1000} means success. */
    @Builder.Default
    private int code = 1000;

    @Builder.Default
    private String message = "Success";

    private T data;

    @Builder.Default
    private Instant timestamp = Instant.now();

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder().data(data).build();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder().message(message).data(data).build();
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, T data) {
        return ApiResponse.<T>builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .data(data)
                .build();
    }

    public static ApiResponse<Object> error(ErrorCode errorCode) {
        return error(errorCode, null);
    }
}
