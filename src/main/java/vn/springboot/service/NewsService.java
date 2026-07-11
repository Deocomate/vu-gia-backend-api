package vn.springboot.service;

import vn.springboot.dto.request.news.NewsCreateRequest;
import vn.springboot.dto.request.news.NewsSearchRequest;
import vn.springboot.dto.request.news.NewsUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.news.NewsResponse;
import vn.springboot.entity.enums.ContentStatus;

public interface NewsService {

    PageResponse<NewsResponse> search(NewsSearchRequest request);

    NewsResponse getById(Long id);

    NewsResponse getBySlug(String slug);

    NewsResponse create(NewsCreateRequest request);

    NewsResponse update(Long id, NewsUpdateRequest request);

    NewsResponse updateStatus(Long id, ContentStatus status);

    void delete(Long id);
}
