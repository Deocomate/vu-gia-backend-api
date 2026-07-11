package vn.springboot.dto.response.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.entity.enums.ProductType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {

    private Long id;

    private Long productId;

    /** Snapshot of the product name at order time. */
    private String productName;

    private ProductType productType;

    /** Snapshot of the unit price at order time. */
    private long unitPrice;

    private int quantity;

    private long subtotal;

    private String comboItems;
}
