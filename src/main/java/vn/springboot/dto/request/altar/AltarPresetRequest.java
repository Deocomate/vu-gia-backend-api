package vn.springboot.dto.request.altar;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.common.storage.StorageUrl;

import java.util.List;

/**
 * Full-representation body for both create ({@code POST}) and replace ({@code PUT}) —
 * {@code PUT} replaces {@code items} wholesale (delete-all-then-insert), the simplest correct
 * semantics for a canvas save (see phase-03 "Items are replaced wholesale on PUT"). {@code slug}
 * is optional on create (auto-generated from {@code name} when blank, same convention as
 * {@code AltarModelCreateRequest}); on update, a blank/omitted slug leaves the existing one
 * untouched.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AltarPresetRequest {

    @NotBlank
    @Size(max = 150)
    private String name;

    @Size(max = 200)
    private String slug;

    @NotBlank
    @Size(max = 255)
    @StorageUrl
    private String thumb;

    @NotBlank
    private String description;

    @NotNull
    private Long altarModelSizeId;

    /** {@code null} => the preset isn't tied to a specific glaze style. */
    private Long altarStyleId;

    /** {@code null} => defaults to {@code 0}. */
    private Integer priority;

    /** {@code null} => defaults to {@code true}. */
    private Boolean isActive;

    @NotNull
    @Valid
    @Builder.Default
    private List<AltarPresetItemRequest> items = List.of();
}
