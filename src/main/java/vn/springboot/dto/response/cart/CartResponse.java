package vn.springboot.dto.response.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** The current user's whole cart plus roll-up totals. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponse {

    private List<CartItemResponse> items;

    /** Sum of line quantities. */
    private int totalQuantity;

    /** Sum of line totals. */
    private long totalAmount;
}
