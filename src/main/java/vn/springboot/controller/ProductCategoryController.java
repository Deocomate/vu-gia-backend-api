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
import vn.springboot.dto.request.product.ProductCategoryCreateRequest;
import vn.springboot.dto.request.product.ProductCategorySearchRequest;
import vn.springboot.dto.request.product.ProductCategoryUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.product.ProductCategoryResponse;
import vn.springboot.service.ProductCategoryService;

/**
 * Product category endpoints. Reads are public; writes require staff roles
 * ({@code ADMIN} / {@code SUPERADMIN}).
 */
@RestController
@RequestMapping("/api/product-categories")
@RequiredArgsConstructor
public class ProductCategoryController {

    private final ProductCategoryService productCategoryService;

    @GetMapping
    public ApiResponse<PageResponse<ProductCategoryResponse>> search(
            @ModelAttribute ProductCategorySearchRequest request) {
        return ApiResponse.success(productCategoryService.search(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductCategoryResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(productCategoryService.getById(id));
    }

    /** Public SEO lookup by slug (e.g. /api/product-categories/slug/binh-gom-su). */
    @GetMapping("/slug/{slug}")
    public ApiResponse<ProductCategoryResponse> getBySlug(@PathVariable String slug) {
        return ApiResponse.success(productCategoryService.getBySlug(slug));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<ProductCategoryResponse> create(
            @Valid @RequestBody ProductCategoryCreateRequest request) {
        return ApiResponse.success("Created successfully", productCategoryService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<ProductCategoryResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductCategoryUpdateRequest request) {
        return ApiResponse.success("Updated successfully", productCategoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        productCategoryService.delete(id);
        return ApiResponse.success("Deleted successfully", null);
    }
}
