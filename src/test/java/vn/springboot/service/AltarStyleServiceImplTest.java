package vn.springboot.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.springboot.common.exception.AppException;
import vn.springboot.common.exception.ErrorCode;
import vn.springboot.dto.request.altar.AltarStyleCreateRequest;
import vn.springboot.dto.response.altar.AltarStyleResponse;
import vn.springboot.entity.altar.AltarStyleEntity;
import vn.springboot.mapper.AltarStyleMapper;
import vn.springboot.repository.AltarDesignRepository;
import vn.springboot.repository.AltarPresetRepository;
import vn.springboot.repository.AltarStyleRepository;
import vn.springboot.repository.ProductRepository;
import vn.springboot.service.impl.AltarStyleServiceImpl;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AltarStyleServiceImplTest {

    @Mock
    private AltarStyleRepository altarStyleRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AltarPresetRepository altarPresetRepository;

    @Mock
    private AltarDesignRepository altarDesignRepository;

    @Mock
    private AltarStyleMapper altarStyleMapper;

    @InjectMocks
    private AltarStyleServiceImpl service;

    private AltarStyleEntity entity(String name) {
        return AltarStyleEntity.builder().name(name).thumb("/files/x.jpg").description("desc").build();
    }

    @Test
    void getById_notFound_throws() {
        when(altarStyleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ALTAR_STYLE_NOT_FOUND);
    }

    @Test
    void create_generatesSlug_andDefaultsActive() {
        when(altarStyleRepository.existsBySlug(any())).thenReturn(false);
        when(altarStyleRepository.save(any(AltarStyleEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(altarStyleMapper.toResponse(any(AltarStyleEntity.class)))
                .thenAnswer(inv -> {
                    AltarStyleEntity e = inv.getArgument(0);
                    return AltarStyleResponse.builder().id(1L).name(e.getName()).slug(e.getSlug())
                            .isActive(e.isActive()).build();
                });

        AltarStyleResponse result = service.create(AltarStyleCreateRequest.builder()
                .name("Men lam").thumb("/files/x.jpg").description("desc").build());

        assertThat(result.getSlug()).isEqualTo("men-lam");
        assertThat(result.getIsActive()).isTrue();
    }

    @Test
    void delete_notFound_throws() {
        when(altarStyleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ALTAR_STYLE_NOT_FOUND);
    }

    /**
     * Mandatory delete-nulls-references test: deleting a style that products reference must
     * not orphan them — the FK is bulk-nulled in the same transaction before the style row
     * itself is removed.
     */
    @Test
    void delete_found_nullsProductReferences_beforeDeletingEntity() {
        AltarStyleEntity existing = entity("Men lam");
        existing.setId(1L);
        when(altarStyleRepository.findById(1L)).thenReturn(Optional.of(existing));

        service.delete(1L);

        verify(productRepository).nullifyAltarStyleReferences(1L);
        verify(altarStyleRepository).delete(existing);
    }

    /** A style still referenced by a preset must not be deletable — block with 409, not a raw FK 500. */
    @Test
    void delete_referencedByPreset_rejected_noMutation() {
        AltarStyleEntity existing = entity("Men lam");
        existing.setId(1L);
        when(altarStyleRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(altarPresetRepository.existsByAltarStyle_Id(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ALTAR_STYLE_REFERENCED);

        verify(productRepository, never()).nullifyAltarStyleReferences(any());
        verify(altarStyleRepository, never()).delete(any(AltarStyleEntity.class));
    }

    /** Same guard, but the referencing row is a customer's saved design rather than a preset. */
    @Test
    void delete_referencedByDesign_rejected_noMutation() {
        AltarStyleEntity existing = entity("Men lam");
        existing.setId(1L);
        when(altarStyleRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(altarDesignRepository.existsByAltarStyle_Id(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ALTAR_STYLE_REFERENCED);

        verify(productRepository, never()).nullifyAltarStyleReferences(any());
        verify(altarStyleRepository, never()).delete(any(AltarStyleEntity.class));
    }

    @Test
    void create_explicitSlugConflict_throws() {
        when(altarStyleRepository.existsBySlug("men-lam")).thenReturn(true);

        assertThatThrownBy(() -> service.create(AltarStyleCreateRequest.builder()
                        .name("Men lam").thumb("/files/x.jpg").description("desc").slug("men-lam").build()))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ALTAR_STYLE_SLUG_EXISTED);

        verify(altarStyleRepository, never()).save(any());
    }
}
