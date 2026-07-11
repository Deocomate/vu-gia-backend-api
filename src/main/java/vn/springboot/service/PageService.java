package vn.springboot.service;

import vn.springboot.dto.request.page.PageCreateRequest;
import vn.springboot.dto.request.page.PageSearchRequest;
import vn.springboot.dto.request.page.PageUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.page.PageDetailResponse;

public interface PageService {

    PageResponse<PageDetailResponse> search(PageSearchRequest request);

    PageDetailResponse getById(Long id);

    PageDetailResponse getByKey(String key);

    PageDetailResponse create(PageCreateRequest request);

    PageDetailResponse update(Long id, PageUpdateRequest request);

    void delete(Long id);
}
