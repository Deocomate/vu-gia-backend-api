package vn.springboot.service;

import vn.springboot.dto.request.redirect.RedirectCreateRequest;
import vn.springboot.dto.request.redirect.RedirectSearchRequest;
import vn.springboot.dto.request.redirect.RedirectUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.redirect.RedirectResponse;

public interface RedirectService {

    PageResponse<RedirectResponse> search(RedirectSearchRequest request);

    RedirectResponse getById(Long id);

    RedirectResponse create(RedirectCreateRequest request);

    RedirectResponse update(Long id, RedirectUpdateRequest request);

    void delete(Long id);
}
