package vn.springboot.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.springboot.common.response.ApiResponse;
import vn.springboot.dto.request.product.ProductCategorySearchRequest;
import vn.springboot.dto.request.product.ProductCategoryUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.product.ProductCategoryResponse;
import vn.springboot.service.ProductCategoryService;

/**
 * Product category endpoints. Reads are public. Categories are a fixed set of 6
 * ({@link vn.springboot.entity.enums.CategoryType}) — no create/delete, only
 * {@code ADMIN}/{@code SUPERADMIN} can update a category's content/SEO fields.
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

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<ProductCategoryResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductCategoryUpdateRequest request) {
        return ApiResponse.success("Updated successfully", productCategoryService.update(id, request));
    }
}
