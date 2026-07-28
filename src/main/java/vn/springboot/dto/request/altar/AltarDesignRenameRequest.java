package vn.springboot.dto.request.altar;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** PATCH body for renaming a saved design — {@code name} only. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AltarDesignRenameRequest {

    @NotBlank
    @Size(max = 150)
    private String name;
}
