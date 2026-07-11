package vn.springboot.service;

import vn.springboot.dto.request.news.NewsCategoryCreateRequest;
import vn.springboot.dto.request.news.NewsCategorySearchRequest;
import vn.springboot.dto.request.news.NewsCategoryUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.news.NewsCategoryResponse;

public interface NewsCategoryService {

    PageResponse<NewsCategoryResponse> search(NewsCategorySearchRequest request);

    NewsCategoryResponse getById(Long id);

    NewsCategoryResponse getBySlug(String slug);

    NewsCategoryResponse create(NewsCategoryCreateRequest request);

    NewsCategoryResponse update(Long id, NewsCategoryUpdateRequest request);

    void delete(Long id);
}
