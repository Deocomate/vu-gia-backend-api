package vn.springboot.service;

import org.springframework.web.multipart.MultipartFile;
import vn.springboot.dto.response.product.ProductImageResponse;

import java.util.List;

public interface ProductImageService {

    ProductImageResponse addImage(Long productId, MultipartFile file, Integer priority);

    List<ProductImageResponse> listImages(Long productId);

    ProductImageResponse updatePriority(Long productId, Long imageId, Integer priority);

    void deleteImage(Long productId, Long imageId);
}
