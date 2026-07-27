package vn.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.springboot.entity.cart.CartItemEntity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {

    /** All items in a user's cart, oldest first. */
    List<CartItemEntity> findByUser_IdOrderByIdAsc(Long userId);

    /** The single line for a (user, product) pair — cart holds one row per product. */
    Optional<CartItemEntity> findByUser_IdAndProduct_Id(Long userId, Long productId);

    /** Batch lookup for order placement — one query instead of one per line item. */
    List<CartItemEntity> findByUser_IdAndProduct_IdIn(Long userId, Collection<Long> productIds);

    void deleteByUser_Id(Long userId);
}
