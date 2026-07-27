package vn.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.springboot.entity.user.RefreshTokenEntity;
import vn.springboot.entity.user.UserEntity;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    Optional<RefreshTokenEntity> findByToken(String token);

    @Modifying
    @Query("UPDATE RefreshTokenEntity rt SET rt.revoked = true WHERE rt.user = :user AND rt.revoked = false")
    void revokeAllByUser(@Param("user") UserEntity user);

    /**
     * Atomically revokes a single not-yet-revoked token and reports whether it actually
     * flipped a row (0 = someone else already revoked it first). Used instead of a
     * read-then-write revoke to close the race window where two concurrent requests
     * both read {@code revoked = false} before either writes back.
     */
    @Modifying
    @Query("UPDATE RefreshTokenEntity rt SET rt.revoked = true WHERE rt.token = :token AND rt.revoked = false")
    int revokeIfActive(@Param("token") String token);
}
