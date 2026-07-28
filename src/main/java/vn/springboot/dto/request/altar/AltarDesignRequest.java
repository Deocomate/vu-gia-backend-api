package vn.springboot.dto.request.altar;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.common.storage.StorageUrl;

/**
 * Create body for a saved design. {@code items}/{@code accessories} are raw JSON array strings —
 * same convention as {@code ProductCreateRequest#comboProducts} — deserialized and validated
 * server-side (bounds, size cap, referenced-product existence) in
 * {@code AltarDesignServiceImpl#validateDesignPayload} before persisting; never trusted as-is.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AltarDesignRequest {

    @NotBlank
    @Size(max = 150)
    private String name;

    @NotBlank
    @Size(max = 500)
    @StorageUrl
    private String thumb;

    @NotNull
    private Long altarModelSizeId;

    /** {@code null} => the design isn't tied to a specific glaze style. */
    private Long altarStyleId;

    /** Raw JSON array string — canvas item snapshot, same shape as the client reducer state. */
    @NotBlank
    private String items;

    /** Raw JSON array string — {@code [{productId, quantity}]}. */
    @NotBlank
    private String accessories;

    @NotNull
    private Long totalPrice;
}
