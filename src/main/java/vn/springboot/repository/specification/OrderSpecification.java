package vn.springboot.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import vn.springboot.dto.request.order.OrderAdminSearchRequest;
import vn.springboot.dto.request.order.OrderSearchRequest;
import vn.springboot.entity.enums.OrderStatus;
import vn.springboot.entity.enums.PaymentStatus;
import vn.springboot.entity.order.OrderEntity;

import java.time.Instant;

public class OrderSpecification {

    /** Base filters (status / payment status). The owner filter is added separately. */
    public static Specification<OrderEntity> build(OrderSearchRequest request) {
        return Specification.allOf(
                status(request.getStatus()),
                paymentStatus(request.getPaymentStatus()));
    }

    /** Full admin filters across all users. */
    public static Specification<OrderEntity> buildAdmin(OrderAdminSearchRequest request) {
        return Specification.allOf(
                like("orderCode", request.getOrderCode()),
                status(request.getStatus()),
                paymentStatus(request.getPaymentStatus()),
                userId(request.getUserId()),
                like("couponCode", request.getCouponCode()),
                placedFrom(request.getPlacedFrom()),
                placedTo(request.getPlacedTo()));
    }

    /** Restricts results to a single user's orders. */
    public static Specification<OrderEntity> ownedBy(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    private static Specification<OrderEntity> like(String field, String value) {
        return (root, query, cb) -> (value == null || value.trim().isEmpty())
                ? cb.conjunction()
                : cb.like(cb.lower(root.get(field)), "%" + value.toLowerCase() + "%");
    }

    private static Specification<OrderEntity> userId(Long userId) {
        return (root, query, cb) -> userId == null
                ? cb.conjunction()
                : cb.equal(root.get("user").get("id"), userId);
    }

    private static Specification<OrderEntity> placedFrom(Instant from) {
        return (root, query, cb) -> from == null
                ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    private static Specification<OrderEntity> placedTo(Instant to) {
        return (root, query, cb) -> to == null
                ? cb.conjunction()
                : cb.lessThanOrEqualTo(root.get("createdAt"), to);
    }

    private static Specification<OrderEntity> status(OrderStatus status) {
        return (root, query, cb) -> status == null
                ? cb.conjunction()
                : cb.equal(root.get("status"), status);
    }

    private static Specification<OrderEntity> paymentStatus(PaymentStatus paymentStatus) {
        return (root, query, cb) -> paymentStatus == null
                ? cb.conjunction()
                : cb.equal(root.get("paymentStatus"), paymentStatus);
    }
}
