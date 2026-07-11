package vn.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.springboot.entity.faq.FaqEntity;

@Repository
public interface FaqRepository
        extends JpaRepository<FaqEntity, Long>, JpaSpecificationExecutor<FaqEntity> {
}
