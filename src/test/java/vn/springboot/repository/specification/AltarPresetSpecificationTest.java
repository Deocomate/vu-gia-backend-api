package vn.springboot.repository.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;
import vn.springboot.dto.request.altar.AltarPresetSearchRequest;
import vn.springboot.entity.altar.AltarPresetEntity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Exercises the {@link Specification} predicate logic directly (no DB), the layer where
 * "public list only returns active presets" is actually enforced — the request's
 * {@code isActive} default ({@code true}, see {@link AltarPresetSearchRequest}) must translate
 * into a real equality predicate, not a no-op.
 */
@SuppressWarnings("unchecked")
class AltarPresetSpecificationTest {

    @Test
    void build_defaultIsActiveTrue_translatesToEqualsTruePredicate() {
        Root<AltarPresetEntity> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path<Object> isActivePath = mock(Path.class);

        when(root.get("isActive")).thenReturn(isActivePath);
        when(cb.equal(isActivePath, true)).thenReturn(mock(Predicate.class));
        when(cb.conjunction()).thenReturn(mock(Predicate.class));
        when(cb.and(any(Predicate.class), any(Predicate.class))).thenAnswer(inv -> mock(Predicate.class));

        // No altarModelSizeId/altarStyleId filter, isActive left at its default (true).
        AltarPresetSearchRequest request = AltarPresetSearchRequest.builder().build();

        Specification<AltarPresetEntity> spec = AltarPresetSpecification.build(request);
        spec.toPredicate(root, query, cb);

        verify(cb).equal(isActivePath, true);
    }

    @Test
    void build_isActiveExplicitFalse_translatesToEqualsFalsePredicate() {
        Root<AltarPresetEntity> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path<Object> isActivePath = mock(Path.class);

        when(root.get("isActive")).thenReturn(isActivePath);
        when(cb.equal(eq(isActivePath), eq(false))).thenReturn(mock(Predicate.class));
        when(cb.conjunction()).thenReturn(mock(Predicate.class));
        when(cb.and(any(Predicate.class), any(Predicate.class))).thenAnswer(inv -> mock(Predicate.class));

        AltarPresetSearchRequest request = AltarPresetSearchRequest.builder().isActive(false).build();

        Specification<AltarPresetEntity> spec = AltarPresetSpecification.build(request);
        spec.toPredicate(root, query, cb);

        verify(cb).equal(isActivePath, false);
    }
}
