package vn.springboot.service;

import vn.springboot.dto.request.product.ProductCategorySearchRequest;
import vn.springboot.dto.request.product.ProductCategoryUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.product.ProductCategoryResponse;

/** Categories are a fixed set of 6 ({@link vn.springboot.entity.enums.CategoryType}) — no create/delete. */
public interface ProductCategoryService {

    PageResponse<ProductCategoryResponse> search(ProductCategorySearchRequest request);

    ProductCategoryResponse getById(Long id);

    ProductCategoryResponse getBySlug(String slug);

    ProductCategoryResponse update(Long id, ProductCategoryUpdateRequest request);
}
