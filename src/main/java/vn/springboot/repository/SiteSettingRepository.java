package vn.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.springboot.entity.setting.SiteSettingEntity;

public interface SiteSettingRepository extends JpaRepository<SiteSettingEntity, Long> {
}
