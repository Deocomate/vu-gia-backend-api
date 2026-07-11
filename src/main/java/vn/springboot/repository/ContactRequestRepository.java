package vn.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.springboot.entity.contact.ContactRequestEntity;
import vn.springboot.entity.enums.ContactStatus;

@Repository
public interface ContactRequestRepository
        extends JpaRepository<ContactRequestEntity, Long>,
        JpaSpecificationExecutor<ContactRequestEntity> {

    long countByStatus(ContactStatus status);
}
