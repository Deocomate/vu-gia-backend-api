package vn.springboot.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.springboot.common.exception.AppException;
import vn.springboot.common.exception.ErrorCode;
import vn.springboot.dto.request.altar.AltarPlacementRequest;
import vn.springboot.dto.response.altar.AltarPlacementResponse;
import vn.springboot.entity.altar.AltarPlacementEntity;
import vn.springboot.entity.product.ProductEntity;
import vn.springboot.entity.product.ProductImageEntity;
import vn.springboot.mapper.AltarPlacementMapper;
import vn.springboot.repository.AltarPlacementRepository;
import vn.springboot.repository.ProductImageRepository;
import vn.springboot.service.impl.AltarPlacementServiceImpl;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AltarPlacementServiceImplTest {

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private AltarPlacementRepository altarPlacementRepository;

    @Mock
    private AltarPlacementMapper altarPlacementMapper;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private AltarPlacementServiceImpl service;

    private ProductEntity product(Long id) {
        ProductEntity e = ProductEntity.builder().name("Bát hương").slug("bat-huong").build();
        e.setId(id);
        return e;
    }

    private ProductImageEntity image(Long id, ProductEntity product) {
        ProductImageEntity e = ProductImageEntity.builder().url("/files/img.jpg").product(product).build();
        e.setId(id);
        return e;
    }

    private AltarPlacementRequest validRequest() {
        return AltarPlacementRequest.builder()
                .overlayImage("/files/overlay.png")
                .defaultX(0.5)
                .defaultY(0.8)
                .widthCm(30)
                .scaleAdjust(1.2)
                .zIndexOverride(null)
                .flippable(true)
                .build();
    }

    @Test
    void getByProductImage_notOwned_throwsImageNotFound() {
        ProductImageEntity image = image(10L, product(2L));
        when(productImageRepository.findById(10L)).thenReturn(Optional.of(image));

        assertThatThrownBy(() -> service.getByProductImage(1L, 10L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.PRODUCT_IMAGE_NOT_FOUND);
    }

    @Test
    void getByProductImage_noPlacement_throwsPlacementNotFound() {
        ProductImageEntity image = image(10L, product(1L));
        when(productImageRepository.findById(10L)).thenReturn(Optional.of(image));
        when(altarPlacementRepository.findByProductImageId(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByProductImage(1L, 10L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ALTAR_PLACEMENT_NOT_FOUND);
    }

    @Test
    void upsert_noExistingPlacement_createsNewLinkedToImage() {
        ProductImageEntity image = image(10L, product(1L));
        when(productImageRepository.findById(10L)).thenReturn(Optional.of(image));
        when(altarPlacementRepository.findByProductImageId(10L)).thenReturn(Optional.empty());
        when(altarPlacementRepository.save(any(AltarPlacementEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(altarPlacementMapper.toResponse(any(AltarPlacementEntity.class)))
                .thenReturn(AltarPlacementResponse.builder().id(100L).build());

        AltarPlacementResponse response = service.upsert(1L, 10L, validRequest());

        ArgumentCaptor<AltarPlacementEntity> captor = ArgumentCaptor.forClass(AltarPlacementEntity.class);
        verify(altarPlacementRepository).save(captor.capture());
        assertThat(captor.getValue().getProductImage()).isSameAs(image);
        assertThat(captor.getValue().getOverlayImage()).isEqualTo("/files/overlay.png");
        assertThat(captor.getValue().getScaleAdjust()).isEqualTo(1.2);
        assertThat(captor.getValue().getZIndexOverride()).isNull();
        assertThat(response.getId()).isEqualTo(100L);
    }

    @Test
    void upsert_existingPlacement_replacesInPlace_doesNotCreateSecondRow() {
        ProductImageEntity image = image(10L, product(1L));
        AltarPlacementEntity existing = AltarPlacementEntity.builder().productImage(image).build();
        existing.setId(55L);
        when(productImageRepository.findById(10L)).thenReturn(Optional.of(image));
        when(altarPlacementRepository.findByProductImageId(10L)).thenReturn(Optional.of(existing));
        when(altarPlacementRepository.save(any(AltarPlacementEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(altarPlacementMapper.toResponse(any(AltarPlacementEntity.class)))
                .thenReturn(AltarPlacementResponse.builder().id(55L).build());

        service.upsert(1L, 10L, validRequest());

        ArgumentCaptor<AltarPlacementEntity> captor = ArgumentCaptor.forClass(AltarPlacementEntity.class);
        verify(altarPlacementRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(55L);
    }

    @Test
    void upsert_scaleAdjustAndFlippableNull_fallBackToDefaults() {
        ProductImageEntity image = image(10L, product(1L));
        when(productImageRepository.findById(10L)).thenReturn(Optional.of(image));
        when(altarPlacementRepository.findByProductImageId(10L)).thenReturn(Optional.empty());
        when(altarPlacementRepository.save(any(AltarPlacementEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(altarPlacementMapper.toResponse(any(AltarPlacementEntity.class)))
                .thenReturn(AltarPlacementResponse.builder().build());

        AltarPlacementRequest request = AltarPlacementRequest.builder()
                .overlayImage("/files/overlay.png")
                .defaultX(0.5).defaultY(0.8).widthCm(30)
                .build();

        service.upsert(1L, 10L, request);

        ArgumentCaptor<AltarPlacementEntity> captor = ArgumentCaptor.forClass(AltarPlacementEntity.class);
        verify(altarPlacementRepository).save(captor.capture());
        assertThat(captor.getValue().getScaleAdjust()).isEqualTo(1.0);
        assertThat(captor.getValue().isFlippable()).isTrue();
    }

    @Test
    void delete_removesRowAndOverlayFile() {
        ProductImageEntity image = image(10L, product(1L));
        AltarPlacementEntity existing = AltarPlacementEntity.builder()
                .productImage(image).overlayImage("/files/overlay.png").build();
        existing.setId(55L);
        when(productImageRepository.findById(10L)).thenReturn(Optional.of(image));
        when(altarPlacementRepository.findByProductImageId(10L)).thenReturn(Optional.of(existing));

        service.delete(1L, 10L);

        verify(fileStorageService).delete("/files/overlay.png");
        verify(altarPlacementRepository).delete(existing);
    }

    @Test
    void delete_noPlacement_throwsNotFound_doesNotTouchStorage() {
        ProductImageEntity image = image(10L, product(1L));
        when(productImageRepository.findById(10L)).thenReturn(Optional.of(image));
        when(altarPlacementRepository.findByProductImageId(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(1L, 10L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ALTAR_PLACEMENT_NOT_FOUND);

        verify(fileStorageService, never()).delete(any());
        verify(altarPlacementRepository, never()).delete(any(AltarPlacementEntity.class));
    }
}
