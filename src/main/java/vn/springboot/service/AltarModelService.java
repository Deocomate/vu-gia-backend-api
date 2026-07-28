package vn.springboot.service;

import vn.springboot.dto.request.altar.AltarModelCreateRequest;
import vn.springboot.dto.request.altar.AltarModelSearchRequest;
import vn.springboot.dto.request.altar.AltarModelSizeCreateRequest;
import vn.springboot.dto.request.altar.AltarModelSizeUpdateRequest;
import vn.springboot.dto.request.altar.AltarModelUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.altar.AltarModelResponse;
import vn.springboot.dto.response.altar.AltarModelSizeResponse;

public interface AltarModelService {

    PageResponse<AltarModelResponse> search(AltarModelSearchRequest request);

    AltarModelResponse getById(Long id);

    AltarModelResponse create(AltarModelCreateRequest request);

    AltarModelResponse update(Long id, AltarModelUpdateRequest request);

    /** Deletes the model and cascades the delete to all its sizes. */
    void delete(Long id);

    AltarModelSizeResponse createSize(Long modelId, AltarModelSizeCreateRequest request);

    AltarModelSizeResponse updateSize(Long modelId, Long sizeId, AltarModelSizeUpdateRequest request);

    void deleteSize(Long modelId, Long sizeId);
}
