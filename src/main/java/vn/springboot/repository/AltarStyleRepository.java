package vn.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.springboot.entity.altar.AltarStyleEntity;

@Repository
public interface AltarStyleRepository
        extends JpaRepository<AltarStyleEntity, Long>, JpaSpecificationExecutor<AltarStyleEntity> {

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);
}
