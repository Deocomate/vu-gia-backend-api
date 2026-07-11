package vn.springboot.dto.request.contact;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.entity.enums.ContactStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactRequestSearchRequest {

    private String name;

    private String email;

    private String phone;

    private ContactStatus status;

    /** 1-based page number (1 = first page). */
    @Builder.Default
    private int page = 1;

    @Builder.Default
    private int size = 10;

    @Builder.Default
    private String sortBy = "id";

    @Builder.Default
    private String sortDirection = "ASC";
}
