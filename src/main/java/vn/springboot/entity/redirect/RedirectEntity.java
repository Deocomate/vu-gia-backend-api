package vn.springboot.entity.redirect;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import vn.springboot.common.entity.BaseEntity;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Table(name = "redirects")
public class RedirectEntity extends BaseEntity {

    @Column(name = "from_path", unique = true, nullable = false, length = 500)
    private String fromPath;

    @Column(name = "to_path", nullable = false, length = 500)
    private String toPath;

    @Builder.Default
    @Column(name = "status_code", nullable = false)
    private int statusCode = 301;

    @Builder.Default
    @Column(name = "hit_count", nullable = false)
    private int hitCount = 0;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
