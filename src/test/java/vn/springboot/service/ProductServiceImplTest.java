package vn.springboot.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import vn.springboot.common.exception.AppException;
import vn.springboot.common.exception.ErrorCode;
import vn.springboot.dto.request.product.ProductCreateRequest;
import vn.springboot.dto.request.product.ProductSearchRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.product.ProductResponse;
import vn.springboot.entity.enums.ProductStatus;
import vn.springboot.entity.enums.ProductType;
import vn.springboot.entity.product.ProductCategoryEntity;
import vn.springboot.entity.product.ProductEntity;
import vn.springboot.mapper.ProductMapper;
import vn.springboot.repository.ProductCategoryRepository;
import vn.springboot.repository.ProductImageRepository;
import vn.springboot.repository.ProductRepository;
import vn.springboot.service.impl.ProductServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductCategoryRepository productCategoryRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private ProductServiceImpl service;

    private ProductEntity productEntity(String name, String slug) {
        return ProductEntity.builder()
                .name(name)
                .thumb("http://img/x.png")
                .type(ProductType.SINGLE)
                .price(1000L)
                .slug(slug)
                .build();
    }

    private ProductResponse response(Long id, String name, String slug) {
        return ProductResponse.builder().id(id).name(name).slug(slug).build();
    }

    private ProductCreateRequest.ProductCreateRequestBuilder validCreate() {
        return ProductCreateRequest.builder()
                .name("Phone X")
                .thumb("http://img/x.png")
                .type(ProductType.SINGLE)
                .price(1000L)
                .productCategoryId(5L);
    }

    @Test
    void search_returnsPageResponse_withoutImages() {
        ProductEntity e = productEntity("Phone X", "phone-x");
        PageImpl<ProductEntity> page = new PageImpl<>(List.of(e), PageRequest.of(0, 10), 1);
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(productMapper.toResponse(e)).thenReturn(response(1L, "Phone X", "phone-x"));

        PageResponse<ProductResponse> result = service.search(ProductSearchRequest.builder().build());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getSlug()).isEqualTo("phone-x");
        assertThat(result.getContent().get(0).getImages()).isNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getPageNumber()).isEqualTo(1);
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isTrue();
        verify(productImageRepository, never()).findByProductIdOrderByPriorityAscIdAsc(any());
    }

    @Test
    void search_clampsSize_andWhitelistsSort() {
        PageImpl<ProductEntity> page = new PageImpl<>(List.of(), PageRequest.of(0, 100), 0);
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        ProductSearchRequest request = ProductSearchRequest.builder()
                .page(1).size(500).sortBy("hacker; DROP TABLE").sortDirection("DESC")
                .build();
        service.search(request);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(productRepository).findAll(any(Specification.class), captor.capture());
        Pageable used = captor.getValue();
        assertThat(used.getPageSize()).isEqualTo(100);
        Sort.Order order = used.getSort().getOrderFor("id");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void getById_notFound_throws() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    void create_categoryNotFound_throws() {
        when(productCategoryRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(validCreate().build()))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.PRODUCT_CATEGORY_NOT_FOUND);

        verify(productRepository, never()).save(any());
    }

    @Test
    void create_clientSlugConflict_throws() {
        when(productCategoryRepository.findById(5L))
                .thenReturn(Optional.of(new ProductCategoryEntity()));
        when(productRepository.existsBySlug("phone-x")).thenReturn(true);

        assertThatThrownBy(() -> service.create(validCreate().slug("phone-x").build()))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.PRODUCT_SLUG_EXISTED);

        verify(productRepository, never()).save(any());
    }

    @Test
    void create_skuConflict_throws() {
        when(productCategoryRepository.findById(5L))
                .thenReturn(Optional.of(new ProductCategoryEntity()));
        when(productRepository.existsBySlug(anyString())).thenReturn(false);
        when(productRepository.existsBySku("SKU-1")).thenReturn(true);

        assertThatThrownBy(() -> service.create(validCreate().sku("SKU-1").build()))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.PRODUCT_SKU_EXISTED);

        verify(productRepository, never()).save(any());
    }

    @Test
    void create_persistsInlineGalleryImages() {
        when(productCategoryRepository.findById(5L))
                .thenReturn(Optional.of(new ProductCategoryEntity()));
        when(productRepository.existsBySlug(anyString())).thenReturn(false);
        when(productRepository.save(any(ProductEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productImageRepository.findByProductIdOrderByPriorityAscIdAsc(any())).thenReturn(List.of());
        when(productMapper.toResponse(any(ProductEntity.class))).thenReturn(response(1L, "Phone X", "phone-x"));

        ProductCreateRequest request = validCreate()
                .images(List.of(
                        vn.springboot.dto.request.product.ProductImageRequest.builder().url("http://img/1.jpg").build(),
                        vn.springboot.dto.request.product.ProductImageRequest.builder().url("http://img/2.jpg").priority(5).build()))
                .build();

        service.create(request);

        ArgumentCaptor<vn.springboot.entity.product.ProductImageEntity> captor =
                ArgumentCaptor.forClass(vn.springboot.entity.product.ProductImageEntity.class);
        verify(productImageRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        assertThat(captor.getAllValues()).extracting("url")
                .containsExactly("http://img/1.jpg", "http://img/2.jpg");
        // priority: first defaults to its index (0), second keeps the supplied 5
        assertThat(captor.getAllValues()).extracting("priority").containsExactly(0, 5);
    }

    @Test
    void updateStatus_changesStatus() {
        ProductEntity e = productEntity("Phone X", "phone-x");
        e.setStatus(ProductStatus.DRAFT);
        when(productRepository.findById(1L)).thenReturn(Optional.of(e));
        when(productRepository.save(any(ProductEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productImageRepository.findByProductIdOrderByPriorityAscIdAsc(any())).thenReturn(List.of());
        when(productMapper.toResponse(any(ProductEntity.class)))
                .thenAnswer(inv -> response(1L, "Phone X", "phone-x"));

        service.updateStatus(1L, ProductStatus.PUBLISHED);

        assertThat(e.getStatus()).isEqualTo(ProductStatus.PUBLISHED);
        verify(productRepository).save(e);
    }
}
