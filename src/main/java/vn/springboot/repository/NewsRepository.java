package vn.springboot.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.springboot.entity.news.NewsEntity;

import java.util.Optional;

@Repository
public interface NewsRepository
        extends JpaRepository<NewsEntity, Long>, JpaSpecificationExecutor<NewsEntity> {

    /**
     * Eager-joins {@code newsCategory} so mapping a page of news to responses
     * doesn't trigger one query per row (N+1). Safe with pagination (to-one join).
     */
    @Override
    @EntityGraph(attributePaths = "newsCategory")
    Page<NewsEntity> findAll(Specification<NewsEntity> spec, Pageable pageable);

    Optional<NewsEntity> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    boolean existsByNewsCategoryId(Long newsCategoryId);

    /** Atomic view-count bump; avoids a read-modify-write race on the hot detail path. */
    @Modifying
    @Query("UPDATE NewsEntity n SET n.viewCount = n.viewCount + 1 WHERE n.id = :id")
    void incrementViewCount(@Param("id") Long id);
}
