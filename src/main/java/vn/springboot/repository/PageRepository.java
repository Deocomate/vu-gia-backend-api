package vn.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.springboot.entity.page.PageEntity;

import java.util.Optional;

@Repository
public interface PageRepository
        extends JpaRepository<PageEntity, Long>, JpaSpecificationExecutor<PageEntity> {

    Optional<PageEntity> findByKey(String key);

    boolean existsByKey(String key);
}
