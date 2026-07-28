package vn.springboot.dto.request.altar;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.common.storage.StorageUrl;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AltarItemGroupCreateRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    /** Optional; auto-generated from {@code name} when blank. */
    @Size(max = 150)
    private String slug;

    @NotBlank
    @Size(max = 255)
    @StorageUrl
    private String thumb;

    /** {@code null} → defaults to {@code true} on create. */
    private Boolean renderOnAltar;

    /** {@code null} → defaults to {@code 0} on create. */
    private Integer priority;

    /** {@code null} → defaults to {@code true} on create. */
    private Boolean isActive;
}
