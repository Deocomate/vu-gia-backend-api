package vn.springboot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.springboot.common.exception.AppException;
import vn.springboot.common.exception.ErrorCode;
import vn.springboot.dto.response.product.ProductImageResponse;
import vn.springboot.entity.product.ProductEntity;
import vn.springboot.entity.product.ProductImageEntity;
import vn.springboot.mapper.ProductMapper;
import vn.springboot.repository.ProductImageRepository;
import vn.springboot.repository.ProductRepository;
import vn.springboot.service.FileStorageService;
import vn.springboot.service.ProductImageService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {

    private static final String IMAGE_FOLDER = "products";

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductMapper productMapper;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public ProductImageResponse addImage(Long productId, MultipartFile file, Integer priority) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        String url = fileStorageService.uploadImage(file, IMAGE_FOLDER);

        ProductImageEntity image = ProductImageEntity.builder()
                .url(url)
                .priority(priority != null ? priority : 0)
                .product(product)
                .build();

        return productMapper.toImageResponse(productImageRepository.save(image));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductImageResponse> listImages(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return productImageRepository.findByProductIdOrderByPriorityAscIdAsc(productId).stream()
                .map(productMapper::toImageResponse)
                .toList();
    }

    @Override
    @Transactional
    public ProductImageResponse updatePriority(Long productId, Long imageId, Integer priority) {
        ProductImageEntity image = loadOwnedImage(productId, imageId);
        image.setPriority(priority != null ? priority : 0);
        return productMapper.toImageResponse(productImageRepository.save(image));
    }

    @Override
    @Transactional
    public void deleteImage(Long productId, Long imageId) {
        ProductImageEntity image = loadOwnedImage(productId, imageId);
        fileStorageService.delete(image.getUrl());
        productImageRepository.delete(image);
    }

    /** Loads an image and asserts it belongs to the given product. */
    private ProductImageEntity loadOwnedImage(Long productId, Long imageId) {
        ProductImageEntity image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_IMAGE_NOT_FOUND));
        if (image.getProduct() == null || !image.getProduct().getId().equals(productId)) {
            throw new AppException(ErrorCode.PRODUCT_IMAGE_NOT_FOUND);
        }
        return image;
    }
}
