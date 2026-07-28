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
    SHIPPING_METHOD_NOT_FOUND(4060, "Shipping method not found", HttpStatus.NOT_FOUND),
    ALTAR_ITEM_GROUP_NOT_FOUND(4061, "Altar item group not found", HttpStatus.NOT_FOUND),
    ALTAR_STYLE_NOT_FOUND(4062, "Altar style not found", HttpStatus.NOT_FOUND),
    ALTAR_MODEL_NOT_FOUND(4063, "Altar model not found", HttpStatus.NOT_FOUND),
    ALTAR_MODEL_SIZE_NOT_FOUND(4064, "Altar model size not found", HttpStatus.NOT_FOUND),
    ALTAR_PLACEMENT_NOT_FOUND(4065, "Altar placement not found", HttpStatus.NOT_FOUND),
    ALTAR_PRESET_NOT_FOUND(4066, "Altar preset not found", HttpStatus.NOT_FOUND),
    ALTAR_DESIGN_NOT_FOUND(4067, "Altar design not found", HttpStatus.NOT_FOUND),

    // 409x - conflict
    USERNAME_EXISTED(4090, "Username already exists", HttpStatus.CONFLICT),
    EMAIL_EXISTED(4091, "Email already exists", HttpStatus.CONFLICT),
    REFRESH_TOKEN_REVOKED(4092, "Refresh token has been revoked", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_EXPIRED(4093, "Refresh token has expired", HttpStatus.UNAUTHORIZED),
    PRODUCT_SLUG_EXISTED(4094, "Product slug already exists", HttpStatus.CONFLICT),
    PRODUCT_SKU_EXISTED(4095, "Product SKU already exists", HttpStatus.CONFLICT),
    PRODUCT_CATEGORY_SLUG_EXISTED(4096, "Product category slug already exists", HttpStatus.CONFLICT),
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
    ORDER_EMPTY(4106, "Order must contain at least one item", HttpStatus.BAD_REQUEST),
    ORDER_NOT_CANCELLABLE(4107, "Order cannot be cancelled in its current status", HttpStatus.CONFLICT),
    SHIPPING_METHOD_CODE_EXISTED(4108, "Shipping method code already exists", HttpStatus.CONFLICT),
    ALTAR_ITEM_GROUP_SLUG_EXISTED(4109, "Altar item group slug already exists", HttpStatus.CONFLICT),
    ALTAR_STYLE_SLUG_EXISTED(4110, "Altar style slug already exists", HttpStatus.CONFLICT),
    ALTAR_MODEL_SLUG_EXISTED(4111, "Altar model slug already exists", HttpStatus.CONFLICT),
    ALTAR_PRESET_SLUG_EXISTED(4112, "Altar preset slug already exists", HttpStatus.CONFLICT),
    ALTAR_PRESET_ITEM_REFERENCED(4113, "Product or image is referenced by an altar preset", HttpStatus.CONFLICT),
    ALTAR_DESIGN_LIMIT_REACHED(4114, "Saved design limit reached (20 max) — delete one before saving another", HttpStatus.CONFLICT),
    ALTAR_MODEL_SIZE_REFERENCED(4115, "Altar model size is referenced by a preset or saved design", HttpStatus.CONFLICT),
    ALTAR_STYLE_REFERENCED(4116, "Altar style is referenced by a preset or saved design", HttpStatus.CONFLICT);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
