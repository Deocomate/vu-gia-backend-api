package vn.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.springboot.entity.redirect.RedirectEntity;

@Repository
public interface RedirectRepository
        extends JpaRepository<RedirectEntity, Long>, JpaSpecificationExecutor<RedirectEntity> {

    boolean existsByFromPath(String fromPath);

    boolean existsByFromPathAndIdNot(String fromPath, Long id);
}
