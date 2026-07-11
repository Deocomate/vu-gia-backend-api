package vn.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.springboot.entity.banner.BannerEntity;

@Repository
public interface BannerRepository
        extends JpaRepository<BannerEntity, Long>, JpaSpecificationExecutor<BannerEntity> {
}
