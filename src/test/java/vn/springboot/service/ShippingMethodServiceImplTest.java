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
import vn.springboot.dto.request.shipping.ShippingMethodCreateRequest;
import vn.springboot.dto.request.shipping.ShippingMethodSearchRequest;
import vn.springboot.dto.request.shipping.ShippingMethodUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.shipping.ShippingMethodResponse;
import vn.springboot.entity.shipping.ShippingMethodEntity;
import vn.springboot.mapper.ShippingMethodMapper;
import vn.springboot.repository.ShippingMethodRepository;
import vn.springboot.service.impl.ShippingMethodServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShippingMethodServiceImplTest {

    @Mock
    private ShippingMethodRepository shippingMethodRepository;

    @Mock
    private ShippingMethodMapper shippingMethodMapper;

    @InjectMocks
    private ShippingMethodServiceImpl service;

    private ShippingMethodEntity entity(String name) {
        return ShippingMethodEntity.builder()
                .name(name)
                .fee(30_000L)
                .build();
    }

    private ShippingMethodResponse response(Long id, String name) {
        return ShippingMethodResponse.builder()
                .id(id)
                .name(name)
                .fee(30_000L)
                .build();
    }

    @Test
    void search_returnsPageResponse() {
        ShippingMethodEntity e = entity("Standard");
        PageImpl<ShippingMethodEntity> page =
                new PageImpl<>(List.of(e), PageRequest.of(0, 10), 1);
        when(shippingMethodRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);
        when(shippingMethodMapper.toResponse(e)).thenReturn(response(1L, "Standard"));

        PageResponse<ShippingMethodResponse> result =
                service.search(ShippingMethodSearchRequest.builder().build());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Standard");
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getPageNumber()).isEqualTo(1);
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isTrue();
    }

    @Test
    void getById_notFound_throws() {
        when(shippingMethodRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.SHIPPING_METHOD_NOT_FOUND);
    }

    @Test
    void create_defaultsSortOrderAndActive() {
        ShippingMethodCreateRequest request = ShippingMethodCreateRequest.builder()
                .name("Standard")
                .fee(30_000L)
                .build();
        when(shippingMethodRepository.save(any(ShippingMethodEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(shippingMethodMapper.toResponse(any(ShippingMethodEntity.class)))
                .thenAnswer(inv -> {
                    ShippingMethodEntity e = inv.getArgument(0);
                    ShippingMethodResponse r = response(1L, e.getName());
                    r.setSortOrder(e.getSortOrder());
                    r.setIsActive(e.isActive());
                    return r;
                });

        ShippingMethodResponse result = service.create(request);

        assertThat(result.getSortOrder()).isEqualTo(0);
        assertThat(result.getIsActive()).isTrue();
        verify(shippingMethodRepository).save(any(ShippingMethodEntity.class));
    }

    @Test
    void update_notFound_throws() {
        when(shippingMethodRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, ShippingMethodUpdateRequest.builder().build()))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.SHIPPING_METHOD_NOT_FOUND);
    }

    @Test
    void update_partialUpdate_onlyChangesProvidedFields() {
        ShippingMethodEntity existing = entity("Standard");
        existing.setId(1L);
        when(shippingMethodRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(shippingMethodRepository.save(any(ShippingMethodEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(shippingMethodMapper.toResponse(any(ShippingMethodEntity.class)))
                .thenAnswer(inv -> {
                    ShippingMethodEntity e = inv.getArgument(0);
                    return response(e.getId(), e.getName());
                });

        service.update(1L, ShippingMethodUpdateRequest.builder().fee(50_000L).build());

        assertThat(existing.getName()).isEqualTo("Standard"); // unchanged
        assertThat(existing.getFee()).isEqualTo(50_000L);     // updated
    }

    @Test
    void delete_notFound_throws() {
        when(shippingMethodRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.SHIPPING_METHOD_NOT_FOUND);
    }

    @Test
    void delete_found_removesEntity() {
        ShippingMethodEntity existing = entity("Standard");
        existing.setId(1L);
        when(shippingMethodRepository.findById(1L)).thenReturn(Optional.of(existing));

        service.delete(1L);

        verify(shippingMethodRepository).delete(existing);
    }
}
