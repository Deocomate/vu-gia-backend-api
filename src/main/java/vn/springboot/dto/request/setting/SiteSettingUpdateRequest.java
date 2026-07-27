package vn.springboot.dto.request.setting;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteSettingUpdateRequest {

    @NotNull(message = "cartEnabled is required")
    private Boolean cartEnabled;
}
