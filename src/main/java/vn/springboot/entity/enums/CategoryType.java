package vn.springboot.entity.enums;

/**
 * Fixed set of storefront product categories. Exactly one {@link vn.springboot.entity.product.ProductCategoryEntity}
 * row exists per value (enforced by a unique DB constraint) — admin can edit each row's content
 * but cannot create or delete categories.
 */
public enum CategoryType {
    BO_DO_THO,
    BINH_PHONG_THUY,
    LUC_BINH_GOM_SU,
    AM_CHEN_BAT_TRANG,
    QUA_TANG_GOM_SU,
    CHUM_SANH_NGAM_RUOU
}
