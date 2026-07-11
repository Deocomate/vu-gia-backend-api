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
import vn.springboot.dto.request.banner.BannerCreateRequest;
import vn.springboot.dto.request.banner.BannerSearchRequest;
import vn.springboot.dto.request.banner.BannerUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.banner.BannerResponse;
import vn.springboot.service.BannerService;

/**
 * Banner endpoints. Reads are public; writes require staff roles
 * ({@code ADMIN} / {@code SUPERADMIN}).
 */
@RestController
@RequestMapping("/api/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    @GetMapping
    public ApiResponse<PageResponse<BannerResponse>> search(
            @ModelAttribute BannerSearchRequest request) {
        return ApiResponse.success(bannerService.search(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<BannerResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(bannerService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<BannerResponse> create(
            @Valid @RequestBody BannerCreateRequest request) {
        return ApiResponse.success("Created successfully", bannerService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<BannerResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody BannerUpdateRequest request) {
        return ApiResponse.success("Updated successfully", bannerService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        bannerService.delete(id);
        return ApiResponse.success("Deleted successfully", null);
    }
}
