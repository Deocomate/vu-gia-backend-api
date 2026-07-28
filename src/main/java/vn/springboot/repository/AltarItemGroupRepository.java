package vn.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.springboot.entity.altar.AltarItemGroupEntity;

@Repository
public interface AltarItemGroupRepository
        extends JpaRepository<AltarItemGroupEntity, Long>, JpaSpecificationExecutor<AltarItemGroupEntity> {

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);
}
