package vn.springboot.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import vn.springboot.dto.request.user.UserSearchRequest;
import vn.springboot.entity.enums.Role;
import vn.springboot.entity.user.UserEntity;

public class UserSpecification {

    public static Specification<UserEntity> build(UserSearchRequest request) {
        return Specification.allOf(
                like("username", request.getUsername()),
                like("email", request.getEmail()),
                like("name", request.getName()),
                like("phone", request.getPhone()),
                hasRole(request.getRole()));
    }

    /** Case-insensitive contains match; no-op when the value is null/blank. */
    private static Specification<UserEntity> like(String field, String value) {
        return (root, query, cb) -> {
            if (value == null || value.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get(field)), "%" + value.toLowerCase() + "%");
        };
    }

    private static Specification<UserEntity> hasRole(Role role) {
        return (root, query, cb) -> {
            if (role == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("role"), role);
        };
    }
}
