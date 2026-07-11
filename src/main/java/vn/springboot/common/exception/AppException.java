package vn.springboot.common.exception;

import lombok.Getter;

/**
 * Application-level exception carrying an {@link ErrorCode}.
 * Throw this anywhere in the service layer; {@link GlobalExceptionHandler}
 * translates it into the standard {@link vn.springboot.common.response.ApiResponse} envelope.
 */
@Getter
public class AppException extends RuntimeException {

    private final ErrorCode errorCode;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public AppException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
