package vn.springboot.service;

import vn.springboot.dto.request.cart.CartItemAddRequest;
import vn.springboot.dto.request.cart.CartItemUpdateRequest;
import vn.springboot.dto.response.cart.CartResponse;

/**
 * Cart operations scoped to the currently authenticated user. Every method
 * returns the whole cart so the client can re-render in one round trip.
 */
public interface CartService {

    CartResponse getMyCart();

    CartResponse addItem(CartItemAddRequest request);

    CartResponse updateItem(Long itemId, CartItemUpdateRequest request);

    CartResponse removeItem(Long itemId);

    CartResponse clear();
}
