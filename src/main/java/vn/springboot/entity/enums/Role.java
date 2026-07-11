package vn.springboot.entity.enums;

/**
 * User role — stored directly on the {@code users.role} ENUM column.
 * Replaces the former roles/permissions tables; authority is derived as
 * {@code ROLE_<name>} (e.g. {@code ROLE_ADMIN}).
 */
public enum Role {
    SUPERADMIN,
    ADMIN,
    CUSTOMER
}
