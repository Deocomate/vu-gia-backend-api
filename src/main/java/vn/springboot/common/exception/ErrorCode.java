package vn.springboot.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Central catalog of business error codes.
 * Each entry maps a stable numeric {@code code} (returned in the JSON body)
 * to a default {@code message} and the {@link HttpStatus} to respond with.
 */
@Getter
public enum ErrorCode {

    // 9xxx - generic / server
    EMAIL_SEND_FAILED(9000, "Failed to send email", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_UPLOAD_FAILED(9001, "Failed to upload file", HttpStatus.INTERNAL_SERVER_ERROR),
    UNCATEGORIZED(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),

    // 4000 - bad request / validation
    INVALID_REQUEST(4000, "Invalid request", HttpStatus.BAD_REQUEST),
    VALIDATION_ERROR(4001, "Validation failed", HttpStatus.BAD_REQUEST),
    INVALID_FILE(4002, "Invalid file", HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE(4003, "Uploaded file is too large", HttpStatus.PAYLOAD_TOO_LARGE),
    INVALID_OLD_PASSWORD(4004, "Current password is incorrect", HttpStatus.BAD_REQUEST),

    // 401x - authentication
    UNAUTHENTICATED(4010, "Authentication required", HttpStatus.UNAUTHORIZED),
    INVALID_CREDENTIALS(4011, "Invalid username or password", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN(4012, "Invalid or malformed token", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(4013, "Token has expired", HttpStatus.UNAUTHORIZED),
    ACCOUNT_DISABLED(4014, "Account is disabled", HttpStatus.UNAUTHORIZED),
    INVALID_GOOGLE_TOKEN(4015, "Invalid Google token", HttpStatus.UNAUTHORIZED),
    GOOGLE_EMAIL_NOT_VERIFIED(4016, "Google email is not verified", HttpStatus.UNAUTHORIZED),

    // 403x - authorization
    ACCESS_DENIED(4030, "You do not have permission to access this resource", HttpStatus.FORBIDDEN),
    CSRF_TOKEN_INVALID(4031, "CSRF token missing or invalid", HttpStatus.FORBIDDEN),

    // 404x - not found
    USER_NOT_FOUND(4040, "User not found", HttpStatus.NOT_FOUND),
    RESOURCE_NOT_FOUND(4042, "Resource not found", HttpStatus.NOT_FOUND),
    REFRESH_TOKEN_NOT_FOUND(4043, "Refresh token not found", HttpStatus.NOT_FOUND),
    PRODUCT_NOT_FOUND(4044, "Product not found", HttpStatus.NOT_FOUND),
    PRODUCT_CATEGORY_NOT_FOUND(4045, "Product category not found", HttpStatus.NOT_FOUND),
    PRODUCT_IMAGE_NOT_FOUND(4046, "Product image not found", HttpStatus.NOT_FOUND),
    BANNER_NOT_FOUND(4047, "Banner not found", HttpStatus.NOT_FOUND),
    SHOWROOM_NOT_FOUND(4048, "Showroom not found", HttpStatus.NOT_FOUND),
    GALLERY_IMAGE_NOT_FOUND(4049, "Gallery image not found", HttpStatus.NOT_FOUND),
    FAQ_NOT_FOUND(4050, "FAQ not found", HttpStatus.NOT_FOUND),
    REDIRECT_NOT_FOUND(4051, "Redirect not found", HttpStatus.NOT_FOUND),
    COUPON_NOT_FOUND(4052, "Coupon not found", HttpStatus.NOT_FOUND),
    NEWS_NOT_FOUND(4053, "News not found", HttpStatus.NOT_FOUND),
    NEWS_CATEGORY_NOT_FOUND(4054, "News category not found", HttpStatus.NOT_FOUND),
    NEWSLETTER_SUBSCRIBER_NOT_FOUND(4055, "Newsletter subscriber not found", HttpStatus.NOT_FOUND),
    CONTACT_REQUEST_NOT_FOUND(4056, "Contact request not found", HttpStatus.NOT_FOUND),
    PAGE_NOT_FOUND(4057, "Page not found", HttpStatus.NOT_FOUND),
    CART_ITEM_NOT_FOUND(4058, "Cart item not found", HttpStatus.NOT_FOUND),
    ORDER_NOT_FOUND(4059, "Order not found", HttpStatus.NOT_FOUND),

    // 409x - conflict
    USERNAME_EXISTED(4090, "Username already exists", HttpStatus.CONFLICT),
    EMAIL_EXISTED(4091, "Email already exists", HttpStatus.CONFLICT),
    REFRESH_TOKEN_REVOKED(4092, "Refresh token has been revoked", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_EXPIRED(4093, "Refresh token has expired", HttpStatus.UNAUTHORIZED),
    PRODUCT_SLUG_EXISTED(4094, "Product slug already exists", HttpStatus.CONFLICT),
    PRODUCT_SKU_EXISTED(4095, "Product SKU already exists", HttpStatus.CONFLICT),
    PRODUCT_CATEGORY_SLUG_EXISTED(4096, "Product category slug already exists", HttpStatus.CONFLICT),
    PRODUCT_CATEGORY_HAS_PRODUCTS(4097, "Product category still has products", HttpStatus.CONFLICT),
    REDIRECT_FROM_PATH_EXISTED(4098, "Redirect from-path already exists", HttpStatus.CONFLICT),
    COUPON_CODE_EXISTED(4099, "Coupon code already exists", HttpStatus.CONFLICT),
    // 409x band full — conflicts continue at 41xx
    NEWS_SLUG_EXISTED(4100, "News slug already exists", HttpStatus.CONFLICT),
    NEWS_CATEGORY_SLUG_EXISTED(4101, "News category slug already exists", HttpStatus.CONFLICT),
    NEWS_CATEGORY_HAS_NEWS(4102, "News category still has news", HttpStatus.CONFLICT),
    NEWSLETTER_EMAIL_EXISTED(4103, "Email is already subscribed", HttpStatus.CONFLICT),
    PAGE_KEY_EXISTED(4104, "Page key already exists", HttpStatus.CONFLICT),
    // Coupon rejected at checkout — carries a specific reason message via AppException(code, message)
    COUPON_NOT_APPLICABLE(4105, "Coupon is not applicable", HttpStatus.CONFLICT),
    ORDER_EMPTY(4106, "Order must contain at least one item", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
