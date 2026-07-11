package vn.springboot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.springboot.common.exception.AppException;
import vn.springboot.common.exception.ErrorCode;
import vn.springboot.dto.request.cart.CartItemAddRequest;
import vn.springboot.dto.request.cart.CartItemUpdateRequest;
import vn.springboot.dto.response.cart.CartItemResponse;
import vn.springboot.dto.response.cart.CartResponse;
import vn.springboot.entity.cart.CartItemEntity;
import vn.springboot.entity.product.ProductEntity;
import vn.springboot.entity.user.UserEntity;
import vn.springboot.mapper.CartItemMapper;
import vn.springboot.repository.CartItemRepository;
import vn.springboot.repository.ProductRepository;
import vn.springboot.security.CustomUserDetails;
import vn.springboot.service.CartService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final CartItemMapper cartItemMapper;

    @Override
    @Transactional(readOnly = true)
    public CartResponse getMyCart() {
        return buildCart(currentUser().getId());
    }

    @Override
    @Transactional
    public CartResponse addItem(CartItemAddRequest request) {
        UserEntity user = currentUser();
        ProductEntity product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        // One row per (user, product): accumulate quantity if the line already exists.
        CartItemEntity item = cartItemRepository
                .findByUser_IdAndProduct_Id(user.getId(), product.getId())
                .orElse(null);

        if (item == null) {
            item = CartItemEntity.builder()
                    .user(user)
                    .product(product)
                    .quantity(request.getQuantity())
                    .comboItems(request.getComboItems())
                    .build();
        } else {
            item.setQuantity(item.getQuantity() + request.getQuantity());
            if (request.getComboItems() != null) {
                item.setComboItems(request.getComboItems());
            }
        }
        cartItemRepository.save(item);

        return buildCart(user.getId());
    }

    @Override
    @Transactional
    public CartResponse updateItem(Long itemId, CartItemUpdateRequest request) {
        UserEntity user = currentUser();
        CartItemEntity item = ownedItem(itemId, user.getId());

        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);

        return buildCart(user.getId());
    }

    @Override
    @Transactional
    public CartResponse removeItem(Long itemId) {
        UserEntity user = currentUser();
        CartItemEntity item = ownedItem(itemId, user.getId());

        cartItemRepository.delete(item);

        return buildCart(user.getId());
    }

    @Override
    @Transactional
    public CartResponse clear() {
        UserEntity user = currentUser();
        cartItemRepository.deleteByUser_Id(user.getId());

        return buildCart(user.getId());
    }

    /** Loads a cart line and asserts it belongs to the given user (else 404). */
    private CartItemEntity ownedItem(Long itemId, Long userId) {
        return cartItemRepository.findById(itemId)
                .filter(item -> item.getUser().getId().equals(userId))
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));
    }

    private CartResponse buildCart(Long userId) {
        List<CartItemResponse> items = cartItemRepository.findByUser_IdOrderByIdAsc(userId).stream()
                .map(cartItemMapper::toResponse)
                .toList();

        int totalQuantity = items.stream().mapToInt(CartItemResponse::getQuantity).sum();
        long totalAmount = items.stream().mapToLong(CartItemResponse::getLineTotal).sum();

        return CartResponse.builder()
                .items(items)
                .totalQuantity(totalQuantity)
                .totalAmount(totalAmount)
                .build();
    }

    private UserEntity currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails principal)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return principal.getUser();
    }
}
