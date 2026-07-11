package vn.springboot.service;

import vn.springboot.dto.request.product.ProductCategoryCreateRequest;
import vn.springboot.dto.request.product.ProductCategorySearchRequest;
import vn.springboot.dto.request.product.ProductCategoryUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.product.ProductCategoryResponse;

public interface ProductCategoryService {

    PageResponse<ProductCategoryResponse> search(ProductCategorySearchRequest request);

    ProductCategoryResponse getById(Long id);

    ProductCategoryResponse getBySlug(String slug);

    ProductCategoryResponse create(ProductCategoryCreateRequest request);

    ProductCategoryResponse update(Long id, ProductCategoryUpdateRequest request);

    void delete(Long id);
}
