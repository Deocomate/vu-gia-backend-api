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
import vn.springboot.dto.request.altar.AltarItemGroupCreateRequest;
import vn.springboot.dto.request.altar.AltarItemGroupSearchRequest;
import vn.springboot.dto.request.altar.AltarItemGroupUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.altar.AltarItemGroupResponse;
import vn.springboot.entity.altar.AltarItemGroupEntity;
import vn.springboot.mapper.AltarItemGroupMapper;
import vn.springboot.repository.AltarItemGroupRepository;
import vn.springboot.repository.ProductRepository;
import vn.springboot.service.impl.AltarItemGroupServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AltarItemGroupServiceImplTest {

    @Mock
    private AltarItemGroupRepository altarItemGroupRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AltarItemGroupMapper altarItemGroupMapper;

    @InjectMocks
    private AltarItemGroupServiceImpl service;

    private AltarItemGroupEntity entity(String name) {
        return AltarItemGroupEntity.builder().name(name).thumb("/files/x.jpg").build();
    }

    private AltarItemGroupResponse response(Long id, String name) {
        return AltarItemGroupResponse.builder().id(id).name(name).build();
    }

    @Test
    void search_returnsPageResponse() {
        AltarItemGroupEntity e = entity("Bộ tam sự");
        PageImpl<AltarItemGroupEntity> page = new PageImpl<>(List.of(e), PageRequest.of(0, 10), 1);
        when(altarItemGroupRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(altarItemGroupMapper.toResponse(e)).thenReturn(response(1L, "Bộ tam sự"));

        PageResponse<AltarItemGroupResponse> result =
                service.search(AltarItemGroupSearchRequest.builder().build());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Bộ tam sự");
    }

    @Test
    void getById_notFound_throws() {
        when(altarItemGroupRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ALTAR_ITEM_GROUP_NOT_FOUND);
    }

    @Test
    void create_generatesSlug_andDefaultsRenderOnAltarAndActive() {
        when(altarItemGroupRepository.existsBySlug(any())).thenReturn(false);
        when(altarItemGroupRepository.save(any(AltarItemGroupEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(altarItemGroupMapper.toResponse(any(AltarItemGroupEntity.class)))
                .thenAnswer(inv -> {
                    AltarItemGroupEntity e = inv.getArgument(0);
                    return AltarItemGroupResponse.builder()
                            .id(1L).name(e.getName()).slug(e.getSlug())
                            .renderOnAltar(e.isRenderOnAltar()).isActive(e.isActive())
                            .build();
                });

        AltarItemGroupResponse result = service.create(
                AltarItemGroupCreateRequest.builder().name("Bộ tam sự").thumb("/files/x.jpg").build());

        assertThat(result.getSlug()).isEqualTo("bo-tam-su");
        assertThat(result.getRenderOnAltar()).isTrue();
        assertThat(result.getIsActive()).isTrue();
    }

    @Test
    void create_explicitSlugConflict_throws() {
        when(altarItemGroupRepository.existsBySlug("bo-tam-su")).thenReturn(true);

        assertThatThrownBy(() -> service.create(AltarItemGroupCreateRequest.builder()
                        .name("Bộ tam sự").thumb("/files/x.jpg").slug("bo-tam-su").build()))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ALTAR_ITEM_GROUP_SLUG_EXISTED);

        verify(altarItemGroupRepository, never()).save(any());
    }

    @Test
    void update_partialUpdate_onlyChangesProvidedFields() {
        AltarItemGroupEntity existing = entity("Bộ tam sự");
        existing.setId(1L);
        existing.setSlug("bo-tam-su");
        when(altarItemGroupRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(altarItemGroupRepository.save(any(AltarItemGroupEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(altarItemGroupMapper.toResponse(any(AltarItemGroupEntity.class)))
                .thenAnswer(inv -> response(1L, ((AltarItemGroupEntity) inv.getArgument(0)).getName()));

        service.update(1L, AltarItemGroupUpdateRequest.builder().priority(5).build());

        assertThat(existing.getName()).isEqualTo("Bộ tam sự"); // unchanged
        assertThat(existing.getPriority()).isEqualTo(5);       // updated
    }

    @Test
    void delete_notFound_throws() {
        when(altarItemGroupRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ALTAR_ITEM_GROUP_NOT_FOUND);
    }

    /**
     * Mandatory delete-nulls-references test: deleting a group that products reference must
     * not orphan them — the FK is bulk-nulled in the same transaction before the group row
     * itself is removed.
     */
    @Test
    void delete_found_nullsProductReferences_beforeDeletingEntity() {
        AltarItemGroupEntity existing = entity("Bộ tam sự");
        existing.setId(1L);
        when(altarItemGroupRepository.findById(1L)).thenReturn(Optional.of(existing));

        service.delete(1L);

        verify(productRepository).nullifyAltarItemGroupReferences(1L);
        verify(altarItemGroupRepository).delete(existing);
    }
}
