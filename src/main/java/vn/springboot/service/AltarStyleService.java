package vn.springboot.service;

import vn.springboot.dto.request.altar.AltarStyleCreateRequest;
import vn.springboot.dto.request.altar.AltarStyleSearchRequest;
import vn.springboot.dto.request.altar.AltarStyleUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.altar.AltarStyleResponse;

public interface AltarStyleService {

    PageResponse<AltarStyleResponse> search(AltarStyleSearchRequest request);

    AltarStyleResponse getById(Long id);

    AltarStyleResponse create(AltarStyleCreateRequest request);

    AltarStyleResponse update(Long id, AltarStyleUpdateRequest request);

    /** Deletes the style; any products referencing it keep existing with the FK nulled out. */
    void delete(Long id);
}
