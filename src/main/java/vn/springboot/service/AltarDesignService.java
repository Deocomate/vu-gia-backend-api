package vn.springboot.service;

import vn.springboot.dto.request.altar.AltarDesignRenameRequest;
import vn.springboot.dto.request.altar.AltarDesignRequest;
import vn.springboot.dto.request.altar.AltarDesignSearchRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.altar.AltarDesignResponse;
import vn.springboot.dto.response.altar.AltarDesignSummaryResponse;

/**
 * Per-user saved-design library ("Lưu vào thư viện"). Every method resolves the caller from the
 * security context and enforces ownership — see {@code AltarDesignServiceImpl} for the exact
 * 404-not-403 contract on foreign ids and the 20-design-per-user cap.
 */
public interface AltarDesignService {

    PageResponse<AltarDesignSummaryResponse> list(AltarDesignSearchRequest request);

    AltarDesignResponse getById(Long id);

    AltarDesignResponse create(AltarDesignRequest request);

    AltarDesignResponse rename(Long id, AltarDesignRenameRequest request);

    void delete(Long id);
}
