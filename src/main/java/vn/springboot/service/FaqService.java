package vn.springboot.service;

import vn.springboot.dto.request.faq.FaqCreateRequest;
import vn.springboot.dto.request.faq.FaqSearchRequest;
import vn.springboot.dto.request.faq.FaqUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.faq.FaqResponse;

public interface FaqService {

    PageResponse<FaqResponse> search(FaqSearchRequest request);

    FaqResponse getById(Long id);

    FaqResponse create(FaqCreateRequest request);

    FaqResponse update(Long id, FaqUpdateRequest request);

    void delete(Long id);
}
