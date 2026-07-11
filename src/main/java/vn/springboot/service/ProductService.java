package vn.springboot.service;

import vn.springboot.dto.request.product.ProductCreateRequest;
import vn.springboot.dto.request.product.ProductSearchRequest;
import vn.springboot.dto.request.product.ProductUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.product.ProductResponse;
import vn.springboot.entity.enums.ProductStatus;

public interface ProductService {

    PageResponse<ProductResponse> search(ProductSearchRequest request);

    ProductResponse getById(Long id);

    ProductResponse getBySlug(String slug);

    ProductResponse create(ProductCreateRequest request);

    ProductResponse update(Long id, ProductUpdateRequest request);

    ProductResponse updateStatus(Long id, ProductStatus status);

    ProductResponse updateFeatured(Long id, boolean featured);

    void delete(Long id);
}
