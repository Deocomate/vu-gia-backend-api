package vn.springboot.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import vn.springboot.dto.request.altar.AltarPresetSearchRequest;
import vn.springboot.entity.altar.AltarPresetEntity;

public class AltarPresetSpecification {

    public static Specification<AltarPresetEntity> build(AltarPresetSearchRequest request) {
        return Specification.allOf(
                altarModelSizeId(request.getAltarModelSizeId()),
                altarStyleId(request.getAltarStyleId()),
                isActive(request.getIsActive()));
    }

    private static Specification<AltarPresetEntity> altarModelSizeId(Long altarModelSizeId) {
        return (root, query, cb) -> {
            if (altarModelSizeId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("altarModelSize").get("id"), altarModelSizeId);
        };
    }

    private static Specification<AltarPresetEntity> altarStyleId(Long altarStyleId) {
        return (root, query, cb) -> {
            if (altarStyleId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("altarStyle").get("id"), altarStyleId);
        };
    }

    private static Specification<AltarPresetEntity> isActive(Boolean isActive) {
        return (root, query, cb) -> {
            if (isActive == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("isActive"), isActive);
        };
    }
}
