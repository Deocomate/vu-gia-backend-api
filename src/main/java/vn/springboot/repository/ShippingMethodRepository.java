package vn.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.springboot.entity.shipping.ShippingMethodEntity;

@Repository
public interface ShippingMethodRepository
        extends JpaRepository<ShippingMethodEntity, Long>, JpaSpecificationExecutor<ShippingMethodEntity> {
}
