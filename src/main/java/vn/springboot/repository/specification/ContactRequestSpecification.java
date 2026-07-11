package vn.springboot.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import vn.springboot.dto.request.contact.ContactRequestSearchRequest;
import vn.springboot.entity.contact.ContactRequestEntity;
import vn.springboot.entity.enums.ContactStatus;

public class ContactRequestSpecification {

    public static Specification<ContactRequestEntity> build(ContactRequestSearchRequest request) {
        return Specification.allOf(
                like("name", request.getName()),
                like("email", request.getEmail()),
                like("phone", request.getPhone()),
                status(request.getStatus()));
    }

    /** Case-insensitive contains match; no-op when the value is null/blank. */
    private static Specification<ContactRequestEntity> like(String field, String value) {
        return (root, query, cb) -> {
            if (value == null || value.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get(field)), "%" + value.toLowerCase() + "%");
        };
    }

    private static Specification<ContactRequestEntity> status(ContactStatus status) {
        return (root, query, cb) -> {
            if (status == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("status"), status);
        };
    }
}
