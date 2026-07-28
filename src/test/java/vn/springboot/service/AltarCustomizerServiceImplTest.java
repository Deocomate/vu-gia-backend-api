package vn.springboot.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.springboot.dto.response.altar.AltarCustomizerItemResponse;
import vn.springboot.dto.response.altar.AltarPlacementResponse;
import vn.springboot.entity.altar.AltarItemGroupEntity;
import vn.springboot.entity.altar.AltarPlacementEntity;
import vn.springboot.entity.enums.CategoryType;
import vn.springboot.entity.enums.ProductStatus;
import vn.springboot.entity.product.ProductEntity;
import vn.springboot.entity.product.ProductImageEntity;
import vn.springboot.mapper.AltarPlacementMapper;
import vn.springboot.repository.AltarPlacementRepository;
import vn.springboot.repository.ProductImageRepository;
import vn.springboot.repository.ProductRepository;
import vn.springboot.service.impl.AltarCustomizerServiceImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Proves boundedness the way this codebase's test tooling allows (no query-count assertion
 * utility exists here, verified by grep): each repository method backing the feed is invoked
 * exactly once per {@code getItems} call, regardless of how many products/images/placements are
 * involved — i.e. no per-product loop issuing its own query (no N+1).
 */
@ExtendWith(MockitoExtension.class)
class AltarCustomizerServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private AltarPlacementRepository altarPlacementRepository;

    @Mock
    private AltarPlacementMapper altarPlacementMapper;

    @InjectMocks
    private AltarCustomizerServiceImpl service;

    private ProductEntity product(Long id, String name, AltarItemGroupEntity group) {
        ProductEntity p = ProductEntity.builder()
                .name(name).slug(name.toLowerCase()).thumb("/files/" + id + ".jpg")
                .price(100_000L).status(ProductStatus.PUBLISHED).altarItemGroup(group)
                .build();
        p.setId(id);
        return p;
    }

    private ProductImageEntity image(Long id, ProductEntity product, int priority) {
        ProductImageEntity img = ProductImageEntity.builder()
                .url("/files/img" + id + ".jpg").product(product).priority(priority).build();
        img.setId(id);
        return img;
    }

    private AltarItemGroupEntity group(Long id, boolean renderOnAltar) {
        AltarItemGroupEntity g = AltarItemGroupEntity.builder()
                .name("Bộ tam sự").slug("bo-tam-su-" + id).thumb("/files/g.jpg")
                .renderOnAltar(renderOnAltar).build();
        g.setId(id);
        return g;
    }

    @Test
    void getItems_noProducts_returnsEmpty_skipsImageAndPlacementQueries() {
        when(productRepository.findAltarCustomizerItems(
                eq(ProductStatus.PUBLISHED), eq(CategoryType.BO_DO_THO), any(), any()))
                .thenReturn(List.of());

        List<AltarCustomizerItemResponse> result = service.getItems(null, null);

        assertThat(result).isEmpty();
        verify(productImageRepository, never()).findByProductIdInOrderByProductIdAscPriorityAscIdAsc(anyList());
        verify(altarPlacementRepository, never()).findByProductImageIdIn(anyList());
    }

    @Test
    void getItems_joinsFirstImagePlacement_andIssuesOneQueryPerRepository() {
        AltarItemGroupEntity group = group(9L, true);
        ProductEntity productA = product(1L, "A", group);
        ProductEntity productB = product(2L, "B", null);

        when(productRepository.findAltarCustomizerItems(
                eq(ProductStatus.PUBLISHED), eq(CategoryType.BO_DO_THO), any(), any()))
                .thenReturn(List.of(productA, productB));

        // Product A has two images; the lower-priority one (id 10) must win as "first".
        ProductImageEntity aFirst = image(10L, productA, 0);
        ProductImageEntity aSecond = image(11L, productA, 5);
        ProductImageEntity bFirst = image(20L, productB, 0);
        when(productImageRepository.findByProductIdInOrderByProductIdAscPriorityAscIdAsc(List.of(1L, 2L)))
                .thenReturn(List.of(aFirst, aSecond, bFirst));

        AltarPlacementEntity placementA = AltarPlacementEntity.builder().productImage(aFirst).build();
        when(altarPlacementRepository.findByProductImageIdIn(List.of(10L, 20L)))
                .thenReturn(List.of(placementA));
        when(altarPlacementMapper.toResponse(placementA))
                .thenReturn(AltarPlacementResponse.builder().productImageId(10L).build());

        List<AltarCustomizerItemResponse> result = service.getItems(null, null);

        assertThat(result).hasSize(2);
        AltarCustomizerItemResponse itemA = result.get(0);
        assertThat(itemA.getProductId()).isEqualTo(1L);
        assertThat(itemA.getPlacement()).isNotNull();
        assertThat(itemA.getPlacement().getProductImageId()).isEqualTo(10L);
        assertThat(itemA.getRenderOnAltar()).isTrue();
        assertThat(itemA.getGroupId()).isEqualTo(9L);

        AltarCustomizerItemResponse itemB = result.get(1);
        assertThat(itemB.getProductId()).isEqualTo(2L);
        assertThat(itemB.getPlacement()).isNull();
        assertThat(itemB.getRenderOnAltar()).isFalse();
        assertThat(itemB.getGroupId()).isNull();

        // Bounded query count: each repository call happens exactly once, not once per product.
        verify(productRepository, times(1)).findAltarCustomizerItems(any(), any(), any(), any());
        verify(productImageRepository, times(1))
                .findByProductIdInOrderByProductIdAscPriorityAscIdAsc(anyList());
        verify(altarPlacementRepository, times(1)).findByProductImageIdIn(anyList());
    }

    @Test
    void getItems_passesGroupAndStyleFiltersThrough() {
        when(productRepository.findAltarCustomizerItems(
                ProductStatus.PUBLISHED, CategoryType.BO_DO_THO, 7L, 8L))
                .thenReturn(List.of());

        service.getItems(7L, 8L);

        verify(productRepository).findAltarCustomizerItems(ProductStatus.PUBLISHED, CategoryType.BO_DO_THO, 7L, 8L);
    }
}
