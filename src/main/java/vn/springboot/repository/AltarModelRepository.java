package vn.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.springboot.entity.altar.AltarModelEntity;

@Repository
public interface AltarModelRepository
        extends JpaRepository<AltarModelEntity, Long>, JpaSpecificationExecutor<AltarModelEntity> {

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);
}
