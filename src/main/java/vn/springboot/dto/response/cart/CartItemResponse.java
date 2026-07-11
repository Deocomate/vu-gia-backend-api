package vn.springboot.dto.response.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponse {

    /** Cart line id (used for update/remove). */
    private Long id;

    private Long productId;

    private String productName;

    private String productThumb;

    private String productSlug;

    /** Current unit price of the product. */
    private long unitPrice;

    private int quantity;

    private String comboItems;

    /** {@code unitPrice * quantity}. */
    private long lineTotal;
}
