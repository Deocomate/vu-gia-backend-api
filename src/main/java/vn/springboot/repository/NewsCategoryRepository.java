package vn.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.springboot.entity.news.NewsCategoryEntity;

import java.util.Optional;

@Repository
public interface NewsCategoryRepository
        extends JpaRepository<NewsCategoryEntity, Long>, JpaSpecificationExecutor<NewsCategoryEntity> {

    Optional<NewsCategoryEntity> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);
}
