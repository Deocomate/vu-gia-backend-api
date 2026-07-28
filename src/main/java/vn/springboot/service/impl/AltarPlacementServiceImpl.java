package vn.springboot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.springboot.common.exception.AppException;
import vn.springboot.common.exception.ErrorCode;
import vn.springboot.dto.request.altar.AltarPlacementRequest;
import vn.springboot.dto.response.altar.AltarPlacementResponse;
import vn.springboot.entity.altar.AltarPlacementEntity;
import vn.springboot.entity.product.ProductImageEntity;
import vn.springboot.mapper.AltarPlacementMapper;
import vn.springboot.repository.AltarPlacementRepository;
import vn.springboot.repository.ProductImageRepository;
import vn.springboot.service.AltarPlacementService;
import vn.springboot.service.FileStorageService;

@Service
@RequiredArgsConstructor
public class AltarPlacementServiceImpl implements AltarPlacementService {

    private final ProductImageRepository productImageRepository;
    private final AltarPlacementRepository altarPlacementRepository;
    private final AltarPlacementMapper altarPlacementMapper;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional(readOnly = true)
    public AltarPlacementResponse getByProductImage(Long productId, Long imageId) {
        loadOwnedImage(productId, imageId);
        return altarPlacementMapper.toResponse(findPlacementOrThrow(imageId));
    }

    @Override
    @Transactional
    public AltarPlacementResponse upsert(Long productId, Long imageId, AltarPlacementRequest request) {
        ProductImageEntity image = loadOwnedImage(productId, imageId);

        AltarPlacementEntity entity = altarPlacementRepository.findByProductImageId(imageId)
                .orElseGet(() -> AltarPlacementEntity.builder().productImage(image).build());

        entity.setOverlayImage(request.getOverlayImage());
        entity.setDefaultX(request.getDefaultX());
        entity.setDefaultY(request.getDefaultY());
        entity.setWidthCm(request.getWidthCm());
        entity.setScaleAdjust(request.getScaleAdjust() != null ? request.getScaleAdjust() : 1.0);
        // Legitimate intentional null: no override means auto-z from defaultY downstream.
        entity.setZIndexOverride(request.getZIndexOverride());
        entity.setFlippable(request.getFlippable() != null ? request.getFlippable() : true);

        return altarPlacementMapper.toResponse(altarPlacementRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long productId, Long imageId) {
        loadOwnedImage(productId, imageId);
        AltarPlacementEntity entity = findPlacementOrThrow(imageId);
        fileStorageService.delete(entity.getOverlayImage());
        altarPlacementRepository.delete(entity);
    }

    private AltarPlacementEntity findPlacementOrThrow(Long imageId) {
        return altarPlacementRepository.findByProductImageId(imageId)
                .orElseThrow(() -> new AppException(ErrorCode.ALTAR_PLACEMENT_NOT_FOUND));
    }

    /** Loads an image and asserts it belongs to the given product — mirrors ProductImageServiceImpl. */
    private ProductImageEntity loadOwnedImage(Long productId, Long imageId) {
        ProductImageEntity image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_IMAGE_NOT_FOUND));
        if (image.getProduct() == null || !image.getProduct().getId().equals(productId)) {
            throw new AppException(ErrorCode.PRODUCT_IMAGE_NOT_FOUND);
        }
        return image;
    }
}
