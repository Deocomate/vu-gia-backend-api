package vn.springboot.seed;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.springboot.repository.CartItemRepository;
import vn.springboot.repository.ContactRequestRepository;
import vn.springboot.repository.OrderItemRepository;
import vn.springboot.repository.OrderRepository;
import vn.springboot.repository.PaymentTransactionRepository;
import vn.springboot.repository.RefreshTokenRepository;

/**
 * Dev-mode-only reset for the 6 tables that hold references into the seeded catalog
 * ({@code user_id}/{@code product_id}/{@code coupon_id}/{@code shipping_method_id}/
 * {@code handled_by_id}) but aren't themselves seeded — they're populated by real
 * interactive usage (logins, cart adds, test orders), so there's no source dataset to
 * reseed them from, only a reset. {@code V1__init_db.sql} declares no real FK
 * constraints, so skipping this wouldn't crash anything; it would just leave a dev
 * session's cart/order/login data silently pointing at catalog rows that no longer
 * exist (or now belong to a different re-seeded entity) after the next restart. Not a
 * {@link DomainSeeder} itself since it has no source data to seed, only to clear.
 */
@Component
@RequiredArgsConstructor
public class TransactionalDataCleaner {

    private final OrderItemRepository orderItemRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ContactRequestRepository contactRequestRepository;

    @Transactional
    public void reset() {
        orderItemRepository.deleteAllInBatch();
        paymentTransactionRepository.deleteAllInBatch();
        orderRepository.deleteAllInBatch();
        cartItemRepository.deleteAllInBatch();
        refreshTokenRepository.deleteAllInBatch();
        contactRequestRepository.deleteAllInBatch();
    }
}
