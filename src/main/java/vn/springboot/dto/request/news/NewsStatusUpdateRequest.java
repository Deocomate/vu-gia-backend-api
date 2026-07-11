package vn.springboot.dto.request.news;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.entity.enums.ContentStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsStatusUpdateRequest {

    @NotNull
    private ContentStatus status;
}
