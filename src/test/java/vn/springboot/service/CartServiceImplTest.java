package vn.springboot.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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
import vn.springboot.service.impl.CartServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartItemMapper cartItemMapper;

    @InjectMocks
    private CartServiceImpl service;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        user = user(1L);
        CustomUserDetails principal = new CustomUserDetails(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private UserEntity user(Long id) {
        UserEntity u = new UserEntity();
        u.setId(id);
        return u;
    }

    private ProductEntity product(Long id, long price) {
        ProductEntity p = new ProductEntity();
        p.setId(id);
        p.setPrice(price);
        return p;
    }

    private CartItemResponse itemResponse(Long id, long unitPrice, int quantity) {
        return CartItemResponse.builder()
                .id(id).unitPrice(unitPrice).quantity(quantity).lineTotal(unitPrice * quantity)
                .build();
    }

    @Test
    void getMyCart_returnsAggregateTotals() {
        CartItemEntity e1 = CartItemEntity.builder().id(1L).user(user).product(product(10L, 100)).quantity(2).build();
        CartItemEntity e2 = CartItemEntity.builder().id(2L).user(user).product(product(11L, 50)).quantity(1).build();
        when(cartItemRepository.findByUser_IdOrderByIdAsc(1L)).thenReturn(List.of(e1, e2));
        when(cartItemMapper.toResponse(e1)).thenReturn(itemResponse(1L, 100, 2));
        when(cartItemMapper.toResponse(e2)).thenReturn(itemResponse(2L, 50, 1));

        CartResponse cart = service.getMyCart();

        assertThat(cart.getItems()).hasSize(2);
        assertThat(cart.getTotalQuantity()).isEqualTo(3);
        assertThat(cart.getTotalAmount()).isEqualTo(250);
    }

    @Test
    void addItem_newProduct_savesNewLine() {
        ProductEntity p = product(10L, 100);
        when(productRepository.findById(10L)).thenReturn(Optional.of(p));
        when(cartItemRepository.findByUser_IdAndProduct_Id(1L, 10L)).thenReturn(Optional.empty());
        when(cartItemRepository.findByUser_IdOrderByIdAsc(1L)).thenReturn(List.of());

        service.addItem(CartItemAddRequest.builder().productId(10L).quantity(2).build());

        verify(cartItemRepository).save(any(CartItemEntity.class));
    }

    @Test
    void addItem_existingProduct_accumulatesQuantity() {
        ProductEntity p = product(10L, 100);
        CartItemEntity existing = CartItemEntity.builder().id(5L).user(user).product(p).quantity(1).build();
        when(productRepository.findById(10L)).thenReturn(Optional.of(p));
        when(cartItemRepository.findByUser_IdAndProduct_Id(1L, 10L)).thenReturn(Optional.of(existing));
        when(cartItemRepository.findByUser_IdOrderByIdAsc(1L)).thenReturn(List.of(existing));
        when(cartItemMapper.toResponse(existing)).thenReturn(itemResponse(5L, 100, 4));

        service.addItem(CartItemAddRequest.builder().productId(10L).quantity(3).build());

        assertThat(existing.getQuantity()).isEqualTo(4);
        verify(cartItemRepository).save(existing);
    }

    @Test
    void addItem_invalidProduct_throws() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addItem(
                CartItemAddRequest.builder().productId(99L).quantity(1).build()))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void updateItem_notOwnedByCurrentUser_throwsNotFound() {
        CartItemEntity foreign = CartItemEntity.builder()
                .id(5L).user(user(2L)).product(product(10L, 100)).quantity(1).build();
        when(cartItemRepository.findById(5L)).thenReturn(Optional.of(foreign));

        assertThatThrownBy(() -> service.updateItem(5L,
                CartItemUpdateRequest.builder().quantity(3).build()))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.CART_ITEM_NOT_FOUND);
        verify(cartItemRepository, never()).save(any());
    }
}
