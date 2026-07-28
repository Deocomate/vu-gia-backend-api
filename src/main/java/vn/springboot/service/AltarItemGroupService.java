package vn.springboot.service;

import vn.springboot.dto.request.altar.AltarItemGroupCreateRequest;
import vn.springboot.dto.request.altar.AltarItemGroupSearchRequest;
import vn.springboot.dto.request.altar.AltarItemGroupUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.altar.AltarItemGroupResponse;

public interface AltarItemGroupService {

    PageResponse<AltarItemGroupResponse> search(AltarItemGroupSearchRequest request);

    AltarItemGroupResponse getById(Long id);

    AltarItemGroupResponse create(AltarItemGroupCreateRequest request);

    AltarItemGroupResponse update(Long id, AltarItemGroupUpdateRequest request);

    /** Deletes the group; any products referencing it keep existing with the FK nulled out. */
    void delete(Long id);
}
