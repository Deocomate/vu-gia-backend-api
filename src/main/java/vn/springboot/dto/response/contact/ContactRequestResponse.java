package vn.springboot.dto.response.contact;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.springboot.entity.enums.ContactStatus;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactRequestResponse {

    private Long id;

    private String name;

    private String email;

    private String phone;

    private String content;

    private ContactStatus status;

    /** Staff member who handled the request (null while status = NEW). */
    private Long handledById;

    private String handledByUsername;

    private Instant createdAt;

    private Instant updatedAt;
}
