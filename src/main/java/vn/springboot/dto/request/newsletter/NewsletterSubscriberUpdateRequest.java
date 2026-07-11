package vn.springboot.dto.request.newsletter;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Admin toggle for a subscriber's active state (activate / unsubscribe). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsletterSubscriberUpdateRequest {

    @NotNull
    private Boolean isActive;
}
