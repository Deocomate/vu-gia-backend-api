package vn.springboot.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.springboot.common.response.ApiResponse;
import vn.springboot.dto.request.news.NewsCategoryCreateRequest;
import vn.springboot.dto.request.news.NewsCategorySearchRequest;
import vn.springboot.dto.request.news.NewsCategoryUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.news.NewsCategoryResponse;
import vn.springboot.service.NewsCategoryService;

/**
 * News category endpoints. Reads are public; writes require staff roles
 * ({@code ADMIN} / {@code SUPERADMIN}).
 */
@RestController
@RequestMapping("/api/news-categories")
@RequiredArgsConstructor
public class NewsCategoryController {

    private final NewsCategoryService newsCategoryService;

    @GetMapping
    public ApiResponse<PageResponse<NewsCategoryResponse>> search(
            @ModelAttribute NewsCategorySearchRequest request) {
        return ApiResponse.success(newsCategoryService.search(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<NewsCategoryResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(newsCategoryService.getById(id));
    }

    /** Public SEO lookup by slug (e.g. /api/news-categories/slug/kien-thuc-gom-su). */
    @GetMapping("/slug/{slug}")
    public ApiResponse<NewsCategoryResponse> getBySlug(@PathVariable String slug) {
        return ApiResponse.success(newsCategoryService.getBySlug(slug));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<NewsCategoryResponse> create(
            @Valid @RequestBody NewsCategoryCreateRequest request) {
        return ApiResponse.success("Created successfully", newsCategoryService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<NewsCategoryResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody NewsCategoryUpdateRequest request) {
        return ApiResponse.success("Updated successfully", newsCategoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        newsCategoryService.delete(id);
        return ApiResponse.success("Deleted successfully", null);
    }
}
