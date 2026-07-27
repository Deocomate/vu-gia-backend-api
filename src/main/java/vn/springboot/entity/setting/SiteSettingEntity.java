package vn.springboot.entity.setting;

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

/**
 * Singleton-row site-wide settings. Always exactly one row (id=1), inserted by
 * {@link vn.springboot.seed.SiteSettingSeeder} on first boot; never created lazily
 * on read to avoid a check-then-act race.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Table(name = "site_setting")
public class SiteSettingEntity extends BaseEntity {

    @Builder.Default
    @Column(name = "cart_enabled", nullable = false)
    private boolean cartEnabled = true;
}
