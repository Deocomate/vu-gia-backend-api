package vn.springboot.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import vn.springboot.common.exception.AppException;
import vn.springboot.common.exception.ErrorCode;
import vn.springboot.dto.request.product.ProductCategorySearchRequest;
import vn.springboot.dto.request.product.ProductCategoryUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.product.ProductCategoryResponse;
import vn.springboot.entity.product.ProductCategoryEntity;
import vn.springboot.mapper.ProductCategoryMapper;
import vn.springboot.repository.ProductCategoryRepository;
import vn.springboot.service.impl.ProductCategoryServiceImpl;

import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductCategoryServiceImplTest {

    @Mock
    private ProductCategoryRepository productCategoryRepository;

    @Mock
    private ProductCategoryMapper productCategoryMapper;

    @InjectMocks
    private ProductCategoryServiceImpl service;

    private ProductCategoryEntity entity(Long id, String name, String slug) {
        return ProductCategoryEntity.builder().name(name).slug(slug).build();
    }

    private ProductCategoryResponse response(Long id, String name, String slug) {
        return ProductCategoryResponse.builder().id(id).name(name).slug(slug).build();
    }

    @Test
    void search_returnsPageResponse() {
        ProductCategoryEntity e = entity(1L, "Phones", "phones");
        PageImpl<ProductCategoryEntity> page =
                new PageImpl<>(List.of(e), PageRequest.of(0, 10), 1);
        when(productCategoryRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);
        when(productCategoryMapper.toResponse(e)).thenReturn(response(1L, "Phones", "phones"));

        PageResponse<ProductCategoryResponse> result =
                service.search(ProductCategorySearchRequest.builder().build());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getSlug()).isEqualTo("phones");
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getPageNumber()).isEqualTo(1);
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isTrue();
    }

    @Test
    void getById_notFound_throws() {
        when(productCategoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.PRODUCT_CATEGORY_NOT_FOUND);
    }

    @Test
    void getBySlug_returnsResponse() {
        ProductCategoryEntity e = entity(1L, "Phones", "phones");
        when(productCategoryRepository.findBySlug("phones")).thenReturn(Optional.of(e));
        when(productCategoryMapper.toResponse(e)).thenReturn(response(1L, "Phones", "phones"));

        ProductCategoryResponse result = service.getBySlug("phones");

        assertThat(result.getSlug()).isEqualTo("phones");
    }

    @Test
    void getBySlug_notFound_throws() {
        when(productCategoryRepository.findBySlug("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getBySlug("missing"))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.PRODUCT_CATEGORY_NOT_FOUND);
    }

    @Test
    void update_appliesPartialFields() {
        ProductCategoryEntity e = entity(1L, "Phones", "phones");
        ProductCategoryUpdateRequest request = ProductCategoryUpdateRequest.builder()
                .name("Bộ đồ thờ cao cấp")
                .build();
        when(productCategoryRepository.findById(1L)).thenReturn(Optional.of(e));
        when(productCategoryRepository.save(any(ProductCategoryEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(productCategoryMapper.toResponse(any(ProductCategoryEntity.class)))
                .thenAnswer(inv -> {
                    ProductCategoryEntity saved = inv.getArgument(0);
                    return response(1L, saved.getName(), saved.getSlug());
                });

        ProductCategoryResponse result = service.update(1L, request);

        assertThat(result.getName()).isEqualTo("Bộ đồ thờ cao cấp");
        assertThat(result.getSlug()).isEqualTo("phones");
    }

    @Test
    void update_slugConflict_throws() {
        ProductCategoryEntity e = entity(1L, "Phones", "phones");
        ProductCategoryUpdateRequest request = ProductCategoryUpdateRequest.builder()
                .slug("binh-phong-thuy")
                .build();
        when(productCategoryRepository.findById(1L)).thenReturn(Optional.of(e));
        when(productCategoryRepository.existsBySlugAndIdNot("binh-phong-thuy", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, request))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.PRODUCT_CATEGORY_SLUG_EXISTED);
    }
}
