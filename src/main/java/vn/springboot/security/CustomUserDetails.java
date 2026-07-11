package vn.springboot.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import vn.springboot.entity.user.UserEntity;

import java.util.Collection;
import java.util.List;

/**
 * Adapts {@link UserEntity} to Spring Security's {@link UserDetails}.
 * The user's single {@link vn.springboot.entity.enums.Role} becomes one
 * authority {@code ROLE_<name>}, enabling {@code hasRole(...)} /
 * {@code hasAnyRole(...)} checks on endpoints.
 */
@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final transient UserEntity user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }
}
