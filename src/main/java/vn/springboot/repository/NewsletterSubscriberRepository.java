package vn.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.springboot.entity.newsletter.NewsletterSubscriberEntity;

@Repository
public interface NewsletterSubscriberRepository
        extends JpaRepository<NewsletterSubscriberEntity, Long>,
        JpaSpecificationExecutor<NewsletterSubscriberEntity> {

    boolean existsByEmail(String email);

    long countByIsActiveTrue();
}
