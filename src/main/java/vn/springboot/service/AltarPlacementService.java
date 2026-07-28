package vn.springboot.service;

import vn.springboot.dto.request.altar.AltarPlacementRequest;
import vn.springboot.dto.response.altar.AltarPlacementResponse;

/** Per-product-image altar placement: find-or-create upsert, not a paged-list CRUD. */
public interface AltarPlacementService {

    /** @throws vn.springboot.common.exception.AppException ALTAR_PLACEMENT_NOT_FOUND if none exists */
    AltarPlacementResponse getByProductImage(Long productId, Long imageId);

    /** Creates the placement if none exists for the image yet, otherwise replaces it in place. */
    AltarPlacementResponse upsert(Long productId, Long imageId, AltarPlacementRequest request);

    /** Removes the placement row and its overlay file. */
    void delete(Long productId, Long imageId);
}
