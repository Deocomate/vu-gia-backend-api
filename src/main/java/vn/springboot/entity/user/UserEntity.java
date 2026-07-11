package vn.springboot.entity.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import vn.springboot.common.entity.BaseEntity;
import vn.springboot.entity.enums.Role;

import java.time.LocalDate;

/**
 * Application account. Mirrors the {@code users} table: local login (username +
 * password) or OAuth (provider + providerId, password null). Authorization is a
 * single {@link Role} stored on the row — no separate roles/permissions tables.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Table(name = "users")
public class UserEntity extends BaseEntity {

    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    /** Null when the account only authenticates through an OAuth provider. */
    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "phone", length = 10)
    private String phone;

    @Column(name = "email", unique = true, nullable = false, length = 50)
    private String email;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "avatar", length = 255)
    private String avatar;

    /** {@code null} = local account; otherwise the OAuth provider, e.g. {@code GOOGLE}. */
    @Column(name = "provider", length = 20)
    private String provider;

    /** The {@code sub}/id returned by the OAuth provider. */
    @Column(name = "provider_id", length = 100)
    private String providerId;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role = Role.CUSTOMER;
}
