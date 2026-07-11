package vn.springboot.service;

import vn.springboot.dto.request.banner.BannerCreateRequest;
import vn.springboot.dto.request.banner.BannerSearchRequest;
import vn.springboot.dto.request.banner.BannerUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.banner.BannerResponse;

public interface BannerService {

    PageResponse<BannerResponse> search(BannerSearchRequest request);

    BannerResponse getById(Long id);

    BannerResponse create(BannerCreateRequest request);

    BannerResponse update(Long id, BannerUpdateRequest request);

    void delete(Long id);
}
