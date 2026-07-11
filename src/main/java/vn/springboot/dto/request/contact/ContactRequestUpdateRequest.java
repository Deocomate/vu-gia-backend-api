package vn.springboot.dto.request.contact;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.entity.enums.ContactStatus;

/** Admin status transition (NEW → HANDLED → CLOSED). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactRequestUpdateRequest {

    @NotNull
    private ContactStatus status;
}
