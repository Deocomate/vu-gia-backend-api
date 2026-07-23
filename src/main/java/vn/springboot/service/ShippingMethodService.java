package vn.springboot.service;

import vn.springboot.dto.request.shipping.ShippingMethodCreateRequest;
import vn.springboot.dto.request.shipping.ShippingMethodSearchRequest;
import vn.springboot.dto.request.shipping.ShippingMethodUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.shipping.ShippingMethodResponse;

public interface ShippingMethodService {

    PageResponse<ShippingMethodResponse> search(ShippingMethodSearchRequest request);

    ShippingMethodResponse getById(Long id);

    ShippingMethodResponse create(ShippingMethodCreateRequest request);

    ShippingMethodResponse update(Long id, ShippingMethodUpdateRequest request);

    void delete(Long id);
}
