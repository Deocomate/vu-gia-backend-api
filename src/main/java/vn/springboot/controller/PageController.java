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
import vn.springboot.dto.request.page.PageCreateRequest;
import vn.springboot.dto.request.page.PageSearchRequest;
import vn.springboot.dto.request.page.PageUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.page.PageDetailResponse;
import vn.springboot.service.PageService;

/**
 * CMS page endpoints. Reads are public (storefront renders pages by key); writes
 * require staff roles ({@code ADMIN} / {@code SUPERADMIN}).
 */
@RestController
@RequestMapping("/api/pages")
@RequiredArgsConstructor
public class PageController {

    private final PageService pageService;

    @GetMapping
    public ApiResponse<PageResponse<PageDetailResponse>> search(
            @ModelAttribute PageSearchRequest request) {
        return ApiResponse.success(pageService.search(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<PageDetailResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(pageService.getById(id));
    }

    @GetMapping("/key/{key}")
    public ApiResponse<PageDetailResponse> getByKey(@PathVariable String key) {
        return ApiResponse.success(pageService.getByKey(key));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<PageDetailResponse> create(
            @Valid @RequestBody PageCreateRequest request) {
        return ApiResponse.success("Created successfully", pageService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<PageDetailResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody PageUpdateRequest request) {
        return ApiResponse.success("Updated successfully", pageService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        pageService.delete(id);
        return ApiResponse.success("Deleted successfully", null);
    }
}
