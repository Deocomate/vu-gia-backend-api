package vn.springboot.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.springboot.common.response.ApiResponse;
import vn.springboot.dto.request.cart.CartItemAddRequest;
import vn.springboot.dto.request.cart.CartItemUpdateRequest;
import vn.springboot.dto.response.cart.CartResponse;
import vn.springboot.service.CartService;

/**
 * Cart endpoints. Every route operates on the authenticated user's own cart —
 * there is no cross-user access, so authentication (any role) is the only guard.
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ApiResponse<CartResponse> getMyCart() {
        return ApiResponse.success(cartService.getMyCart());
    }

    @PostMapping("/items")
    public ApiResponse<CartResponse> addItem(@Valid @RequestBody CartItemAddRequest request) {
        return ApiResponse.success("Added to cart", cartService.addItem(request));
    }

    @PutMapping("/items/{itemId}")
    public ApiResponse<CartResponse> updateItem(
            @PathVariable Long itemId,
            @Valid @RequestBody CartItemUpdateRequest request) {
        return ApiResponse.success("Updated successfully", cartService.updateItem(itemId, request));
    }

    @DeleteMapping("/items/{itemId}")
    public ApiResponse<CartResponse> removeItem(@PathVariable Long itemId) {
        return ApiResponse.success("Removed from cart", cartService.removeItem(itemId));
    }

    @DeleteMapping
    public ApiResponse<CartResponse> clear() {
        return ApiResponse.success("Cart cleared", cartService.clear());
    }
}
