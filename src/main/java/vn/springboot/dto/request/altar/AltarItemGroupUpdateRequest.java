package vn.springboot.dto.request.altar;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.common.storage.StorageUrl;

/** Partial update: mọi field optional; field null → giữ nguyên, không ghi đè. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AltarItemGroupUpdateRequest {

    @Size(max = 100)
    private String name;

    @Size(max = 150)
    private String slug;

    @Size(max = 255)
    @StorageUrl
    private String thumb;

    private Boolean renderOnAltar;

    private Integer priority;

    private Boolean isActive;
}
