package vn.springboot.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import vn.springboot.common.exception.AppException;
import vn.springboot.common.exception.ErrorCode;
import vn.springboot.dto.response.product.ProductImageResponse;
import vn.springboot.entity.altar.AltarPlacementEntity;
import vn.springboot.entity.product.ProductEntity;
import vn.springboot.entity.product.ProductImageEntity;
import vn.springboot.mapper.ProductMapper;
import vn.springboot.repository.AltarPlacementRepository;
import vn.springboot.repository.AltarPresetItemRepository;
import vn.springboot.repository.ProductImageRepository;
import vn.springboot.repository.ProductRepository;
import vn.springboot.service.impl.ProductImageServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductImageServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private AltarPlacementRepository altarPlacementRepository;

    @Mock
    private AltarPresetItemRepository altarPresetItemRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private ProductImageServiceImpl service;

    private ProductEntity product(Long id) {
        ProductEntity e = ProductEntity.builder().name("Phone X").slug("phone-x").build();
        e.setId(id);
        return e;
    }

    @Test
    void addImage_uploadsAndPersistsReturnedUrl() {
        ProductEntity product = product(1L);
        MultipartFile file = new MockMultipartFile("file", "pic.png", "image/png", new byte[]{1, 2, 3});
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(fileStorageService.uploadImage(file, "products")).thenReturn("http://cdn/products/pic.png");
        when(productImageRepository.save(any(ProductImageEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productMapper.toImageResponse(any(ProductImageEntity.class)))
                .thenAnswer(inv -> {
                    ProductImageEntity img = inv.getArgument(0);
                    return ProductImageResponse.builder().url(img.getUrl()).priority(img.getPriority()).build();
                });

        ProductImageResponse result = service.addImage(1L, file, 3);

        verify(fileStorageService).uploadImage(file, "products");
        ArgumentCaptor<ProductImageEntity> captor = ArgumentCaptor.forClass(ProductImageEntity.class);
        verify(productImageRepository).save(captor.capture());
        assertThat(captor.getValue().getUrl()).isEqualTo("http://cdn/products/pic.png");
        assertThat(captor.getValue().getPriority()).isEqualTo(3);
        assertThat(captor.getValue().getProduct()).isSameAs(product);
        assertThat(result.getUrl()).isEqualTo("http://cdn/products/pic.png");
    }

    @Test
    void addImage_productNotFound_throws() {
        MultipartFile file = new MockMultipartFile("file", "pic.png", "image/png", new byte[]{1});
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addImage(99L, file, null))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);

        verify(fileStorageService, never()).uploadImage(any(), eq("products"));
    }

    @Test
    void deleteImage_imageNotOwnedByProduct_throws() {
        ProductImageEntity image = ProductImageEntity.builder()
                .url("http://cdn/products/pic.png")
                .product(product(2L))
                .build();
        image.setId(10L);
        when(productImageRepository.findById(10L)).thenReturn(Optional.of(image));

        assertThatThrownBy(() -> service.deleteImage(1L, 10L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.PRODUCT_IMAGE_NOT_FOUND);

        verify(fileStorageService, never()).delete(any());
        verify(productImageRepository, never()).delete(any());
    }

    /**
     * Cascade-delete: an image with a placement must have both the placement row and its
     * overlay file removed — otherwise every deleted image leaks an orphaned overlay on disk.
     */
    @Test
    void deleteImage_withPlacement_deletesPlacementRowAndOverlayFile_noOrphans() {
        ProductImageEntity image = ProductImageEntity.builder()
                .url("/files/products/pic.png")
                .product(product(1L))
                .build();
        image.setId(10L);
        AltarPlacementEntity placement = AltarPlacementEntity.builder()
                .overlayImage("/files/altar-overlays/overlay.png")
                .build();
        placement.setId(99L);
        when(productImageRepository.findById(10L)).thenReturn(Optional.of(image));
        when(altarPlacementRepository.findByProductImageId(10L)).thenReturn(Optional.of(placement));

        service.deleteImage(1L, 10L);

        verify(fileStorageService).delete("/files/altar-overlays/overlay.png");
        verify(altarPlacementRepository).delete(placement);
        verify(fileStorageService).delete("/files/products/pic.png");
        verify(productImageRepository).delete(image);
    }

    /** No placement attached — deletion must not touch the placement repository/storage at all. */
    @Test
    void deleteImage_withoutPlacement_onlyDeletesImage_noPlacementSideEffects() {
        ProductImageEntity image = ProductImageEntity.builder()
                .url("/files/products/pic.png")
                .product(product(1L))
                .build();
        image.setId(10L);
        when(productImageRepository.findById(10L)).thenReturn(Optional.of(image));
        when(altarPlacementRepository.findByProductImageId(10L)).thenReturn(Optional.empty());

        service.deleteImage(1L, 10L);

        verify(altarPlacementRepository, never()).delete(any(AltarPlacementEntity.class));
        verify(fileStorageService).delete("/files/products/pic.png");
        verify(productImageRepository).delete(image);
    }

    /** Blocks deletion instead of leaving a preset item pointing at a removed image. */
    @Test
    void deleteImage_referencedByAltarPreset_throws409_namingPreset_touchesNothing() {
        ProductImageEntity image = ProductImageEntity.builder()
                .url("/files/products/pic.png")
                .product(product(1L))
                .build();
        image.setId(10L);
        when(productImageRepository.findById(10L)).thenReturn(Optional.of(image));
        when(altarPresetItemRepository.existsByProductImageId(10L)).thenReturn(true);
        when(altarPresetItemRepository.findDistinctPresetNamesByProductImageId(10L))
                .thenReturn(List.of("Bộ gợi ý A"));

        assertThatThrownBy(() -> service.deleteImage(1L, 10L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ALTAR_PRESET_ITEM_REFERENCED);
        assertThatThrownBy(() -> service.deleteImage(1L, 10L)).hasMessageContaining("Bộ gợi ý A");

        verify(altarPlacementRepository, never()).findByProductImageId(any());
        verify(fileStorageService, never()).delete(any());
        verify(productImageRepository, never()).delete(any());
    }
}
