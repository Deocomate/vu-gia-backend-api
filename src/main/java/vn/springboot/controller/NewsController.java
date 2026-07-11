package vn.springboot.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.springboot.common.response.ApiResponse;
import vn.springboot.dto.request.news.NewsCreateRequest;
import vn.springboot.dto.request.news.NewsSearchRequest;
import vn.springboot.dto.request.news.NewsStatusUpdateRequest;
import vn.springboot.dto.request.news.NewsUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.news.NewsResponse;
import vn.springboot.service.NewsService;

/**
 * News endpoints. Reads are public; writes require staff roles
 * ({@code ADMIN} / {@code SUPERADMIN}).
 */
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @GetMapping
    public ApiResponse<PageResponse<NewsResponse>> search(@ModelAttribute NewsSearchRequest request) {
        return ApiResponse.success(newsService.search(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<NewsResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(newsService.getById(id));
    }

    @GetMapping("/slug/{slug}")
    public ApiResponse<NewsResponse> getBySlug(@PathVariable String slug) {
        return ApiResponse.success(newsService.getBySlug(slug));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<NewsResponse> create(@Valid @RequestBody NewsCreateRequest request) {
        return ApiResponse.success("Created successfully", newsService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<NewsResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody NewsUpdateRequest request) {
        return ApiResponse.success("Updated successfully", newsService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<NewsResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody NewsStatusUpdateRequest request) {
        return ApiResponse.success("Updated successfully", newsService.updateStatus(id, request.getStatus()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        newsService.delete(id);
        return ApiResponse.success("Deleted successfully", null);
    }
}
