package vn.springboot.service;

import vn.springboot.dto.request.altar.AltarPresetRequest;
import vn.springboot.dto.request.altar.AltarPresetSearchRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.altar.AltarPresetResponse;

public interface AltarPresetService {

    PageResponse<AltarPresetResponse> search(AltarPresetSearchRequest request);

    AltarPresetResponse getById(Long id);

    AltarPresetResponse create(AltarPresetRequest request);

    /** Replaces the preset's items wholesale (delete-all-then-insert). */
    AltarPresetResponse update(Long id, AltarPresetRequest request);

    /** Deletes the preset and cascades the delete to all its items. */
    void delete(Long id);
}
