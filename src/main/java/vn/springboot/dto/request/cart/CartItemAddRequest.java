package vn.springboot.dto.request.cart;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Add a product to the current user's cart (quantity accumulates if it already exists). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemAddRequest {

    @NotNull
    private Long productId;

    @NotNull
    @Positive
    private Integer quantity;

    /** JSON array of chosen sub-items; only meaningful when the product is a COMBO. */
    private String comboItems;
}
