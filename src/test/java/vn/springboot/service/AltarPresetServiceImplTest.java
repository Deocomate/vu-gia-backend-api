package vn.springboot.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.springboot.common.exception.AppException;
import vn.springboot.common.exception.ErrorCode;
import vn.springboot.dto.request.altar.AltarPresetItemRequest;
import vn.springboot.dto.request.altar.AltarPresetRequest;
import vn.springboot.dto.response.altar.AltarPresetResponse;
import vn.springboot.entity.altar.AltarModelSizeEntity;
import vn.springboot.entity.altar.AltarPresetEntity;
import vn.springboot.entity.altar.AltarPresetItemEntity;
import vn.springboot.entity.enums.ProductType;
import vn.springboot.entity.product.ProductEntity;
import vn.springboot.entity.product.ProductImageEntity;
import vn.springboot.mapper.AltarPlacementMapper;
import vn.springboot.mapper.AltarPresetMapper;
import vn.springboot.repository.AltarModelSizeRepository;
import vn.springboot.repository.AltarPlacementRepository;
import vn.springboot.repository.AltarPresetItemRepository;
import vn.springboot.repository.AltarPresetRepository;
import vn.springboot.repository.AltarStyleRepository;
import vn.springboot.repository.ProductImageRepository;
import vn.springboot.repository.ProductRepository;
import vn.springboot.service.impl.AltarPresetServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AltarPresetServiceImplTest {

    @Mock
    private AltarPresetRepository altarPresetRepository;

    @Mock
    private AltarPresetItemRepository altarPresetItemRepository;

    @Mock
    private AltarModelSizeRepository altarModelSizeRepository;

    @Mock
    private AltarStyleRepository altarStyleRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private AltarPlacementRepository altarPlacementRepository;

    @Mock
    private AltarPresetMapper altarPresetMapper;

    @Mock
    private AltarPlacementMapper altarPlacementMapper;

    @InjectMocks
    private AltarPresetServiceImpl service;

    private AltarModelSizeEntity altarModelSize(Long id) {
        AltarModelSizeEntity e = AltarModelSizeEntity.builder().build();
        e.setId(id);
        return e;
    }

    private ProductEntity product(Long id) {
        ProductEntity e = ProductEntity.builder()
                .name("Bát hương").slug("bat-huong").thumb("/files/x.jpg")
                .type(ProductType.SINGLE).price(1000L).build();
        e.setId(id);
        return e;
    }

    private ProductImageEntity image(Long id, ProductEntity product) {
        ProductImageEntity e = ProductImageEntity.builder().url("/files/img.jpg").product(product).build();
        e.setId(id);
        return e;
    }

    private AltarPresetEntity presetEntity(Long id) {
        AltarPresetEntity e = AltarPresetEntity.builder()
                .name("Bộ gợi ý A").thumb("/files/thumb.jpg").description("desc")
                .build();
        e.setId(id);
        e.setSlug("bo-goi-y-a");
        return e;
    }

    private AltarPresetRequest.AltarPresetRequestBuilder validPresetRequest() {
        return AltarPresetRequest.builder()
                .name("Bộ gợi ý A")
                .slug("bo-goi-y-a")
                .thumb("/files/thumb.jpg")
                .description("desc")
                .altarModelSizeId(1L);
    }

    /** FK resolution + persistence stubs common to every create() call that reaches {@code save}. */
    private void stubHappyPathFks() {
        when(altarModelSizeRepository.findById(1L)).thenReturn(Optional.of(altarModelSize(1L)));
        when(altarPresetRepository.save(any(AltarPresetEntity.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void create_canvasItemMissingCoords_throwsValidationError() {
        stubHappyPathFks();
        when(altarPresetRepository.existsBySlug("bo-goi-y-a")).thenReturn(false);

        AltarPresetItemRequest item = AltarPresetItemRequest.builder()
                .productId(5L).productImageId(20L).quantity(1).build(); // x/y both null

        AltarPresetRequest request = validPresetRequest().items(List.of(item)).build();

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.VALIDATION_ERROR);

        verify(altarPresetItemRepository, never()).saveAll(any());
    }

    @Test
    void create_accessoryItemWithCoords_throwsValidationError() {
        stubHappyPathFks();
        when(altarPresetRepository.existsBySlug("bo-goi-y-a")).thenReturn(false);

        AltarPresetItemRequest item = AltarPresetItemRequest.builder()
                .productId(5L).quantity(1).x(0.5).y(0.5).build(); // no productImageId, but has coords

        AltarPresetRequest request = validPresetRequest().items(List.of(item)).build();

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.VALIDATION_ERROR);

        verify(altarPresetItemRepository, never()).saveAll(any());
    }

    @Test
    void create_validCanvasItem_roundTripsPositionScaleFlipZ() {
        stubHappyPathFks();
        when(altarPresetRepository.existsBySlug("bo-goi-y-a")).thenReturn(false);
        when(altarPresetMapper.toResponse(any(AltarPresetEntity.class)))
                .thenReturn(AltarPresetResponse.builder().build());
        ProductEntity product = product(5L);
        ProductImageEntity productImage = image(20L, product);
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        when(productImageRepository.findById(20L)).thenReturn(Optional.of(productImage));
        when(altarPresetItemRepository.findByPresetIdOrderBySortOrderAscIdAsc(any())).thenReturn(List.of());

        AltarPresetItemRequest item = AltarPresetItemRequest.builder()
                .productId(5L).productImageId(20L).quantity(2)
                .x(0.3).y(0.7).scaleAdjust(1.5).flipped(true).zIndexOverride(9).sortOrder(4)
                .build();

        service.create(validPresetRequest().items(List.of(item)).build());

        ArgumentCaptor<List<AltarPresetItemEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(altarPresetItemRepository).saveAll(captor.capture());
        AltarPresetItemEntity saved = captor.getValue().get(0);
        assertThat(saved.getProduct()).isSameAs(product);
        assertThat(saved.getProductImage()).isSameAs(productImage);
        assertThat(saved.getQuantity()).isEqualTo(2);
        assertThat(saved.getX()).isEqualTo(0.3);
        assertThat(saved.getY()).isEqualTo(0.7);
        assertThat(saved.getScaleAdjust()).isEqualTo(1.5);
        assertThat(saved.isFlipped()).isTrue();
        assertThat(saved.getZIndexOverride()).isEqualTo(9);
        assertThat(saved.getSortOrder()).isEqualTo(4);
    }

    @Test
    void create_itemImageNotOwnedByProduct_throwsImageNotFound() {
        stubHappyPathFks();
        when(altarPresetRepository.existsBySlug("bo-goi-y-a")).thenReturn(false);
        ProductEntity product = product(5L);
        ProductEntity otherProduct = product(6L);
        ProductImageEntity productImage = image(20L, otherProduct);
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        when(productImageRepository.findById(20L)).thenReturn(Optional.of(productImage));

        AltarPresetItemRequest item = AltarPresetItemRequest.builder()
                .productId(5L).productImageId(20L).quantity(1).x(0.3).y(0.7).build();

        assertThatThrownBy(() -> service.create(validPresetRequest().items(List.of(item)).build()))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.PRODUCT_IMAGE_NOT_FOUND);
    }

    @Test
    void update_replacesItemsWholesale_deletesOldInsertsNew() {
        AltarPresetEntity existing = presetEntity(10L);
        ProductEntity product = product(5L);
        AltarPresetItemEntity oldItem = AltarPresetItemEntity.builder()
                .preset(existing).product(product).quantity(1).scaleAdjust(1.0).sortOrder(0).build();
        oldItem.setId(101L);

        when(altarPresetRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(altarModelSizeRepository.findById(1L)).thenReturn(Optional.of(altarModelSize(1L)));
        when(altarPresetRepository.save(any(AltarPresetEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(altarPresetMapper.toResponse(any(AltarPresetEntity.class)))
                .thenReturn(AltarPresetResponse.builder().build());
        when(altarPresetItemRepository.findByPresetIdOrderBySortOrderAscIdAsc(10L))
                .thenReturn(List.of(oldItem), List.of()); // 1st call: existing rows to delete; 2nd: post-replace read
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));

        AltarPresetItemRequest newItem = AltarPresetItemRequest.builder()
                .productId(5L).quantity(3).build(); // accessory: no image/coords

        service.update(10L, validPresetRequest().items(List.of(newItem)).build());

        verify(altarPresetItemRepository).deleteAll(List.of(oldItem));

        ArgumentCaptor<List<AltarPresetItemEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(altarPresetItemRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().get(0).getProduct()).isSameAs(product);
        assertThat(captor.getValue().get(0).getProductImage()).isNull();
        assertThat(captor.getValue().get(0).getQuantity()).isEqualTo(3);
    }

    @Test
    void delete_cascadesDeletesItemsThenPreset() {
        AltarPresetEntity preset = presetEntity(10L);
        AltarPresetItemEntity item = AltarPresetItemEntity.builder().preset(preset).build();
        item.setId(101L);
        when(altarPresetRepository.findById(10L)).thenReturn(Optional.of(preset));
        when(altarPresetItemRepository.findByPresetIdOrderBySortOrderAscIdAsc(10L)).thenReturn(List.of(item));

        service.delete(10L);

        verify(altarPresetItemRepository).deleteAll(List.of(item));
        verify(altarPresetRepository).delete(preset);
    }

    @Test
    void getById_notFound_throws() {
        when(altarPresetRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ALTAR_PRESET_NOT_FOUND);
    }

    @Test
    void create_altarModelSizeNotFound_throws() {
        when(altarModelSizeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(validPresetRequest().build()))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ALTAR_MODEL_SIZE_NOT_FOUND);

        verify(altarPresetRepository, never()).save(any());
    }

    @Test
    void create_slugConflict_throws() {
        when(altarModelSizeRepository.findById(1L)).thenReturn(Optional.of(altarModelSize(1L)));
        when(altarPresetRepository.existsBySlug("bo-goi-y-a")).thenReturn(true);

        assertThatThrownBy(() -> service.create(validPresetRequest().build()))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ALTAR_PRESET_SLUG_EXISTED);

        verify(altarPresetRepository, never()).save(any());
    }
}
