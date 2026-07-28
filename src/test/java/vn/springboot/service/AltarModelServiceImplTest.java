package vn.springboot.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.springboot.common.exception.AppException;
import vn.springboot.common.exception.ErrorCode;
import vn.springboot.dto.request.altar.AltarModelSizeCreateRequest;
import vn.springboot.dto.request.altar.AltarModelSizeUpdateRequest;
import vn.springboot.dto.response.altar.AltarModelResponse;
import vn.springboot.dto.response.altar.AltarModelSizeResponse;
import vn.springboot.entity.altar.AltarModelEntity;
import vn.springboot.entity.altar.AltarModelSizeEntity;
import vn.springboot.mapper.AltarModelMapper;
import vn.springboot.mapper.AltarModelSizeMapper;
import vn.springboot.repository.AltarDesignRepository;
import vn.springboot.repository.AltarModelRepository;
import vn.springboot.repository.AltarModelSizeRepository;
import vn.springboot.repository.AltarPresetRepository;
import vn.springboot.service.impl.AltarModelServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AltarModelServiceImplTest {

    @Mock
    private AltarModelRepository altarModelRepository;

    @Mock
    private AltarModelSizeRepository altarModelSizeRepository;

    @Mock
    private AltarPresetRepository altarPresetRepository;

    @Mock
    private AltarDesignRepository altarDesignRepository;

    @Mock
    private AltarModelMapper altarModelMapper;

    @Mock
    private AltarModelSizeMapper altarModelSizeMapper;

    @InjectMocks
    private AltarModelServiceImpl service;

    private AltarModelEntity modelEntity() {
        AltarModelEntity e = AltarModelEntity.builder()
                .name("Bàn thờ gia tiên").thumb("/files/x.jpg").description("desc").build();
        e.setId(1L);
        e.setSlug("ban-tho-gia-tien");
        return e;
    }

    private AltarModelSizeEntity sizeEntity(Long id, AltarModelEntity model) {
        AltarModelSizeEntity e = AltarModelSizeEntity.builder()
                .altarModel(model)
                .label("127 x 61 cm")
                .widthCm(127)
                .depthCm(61)
                .backgroundImage("/files/bg.jpg")
                .surfaceLeft(0.1).surfaceTop(0.1).surfaceRight(0.9).surfaceBottom(0.9)
                .surfaceWidthCm(120)
                .build();
        e.setId(id);
        return e;
    }

    private AltarModelSizeCreateRequest validSizeCreateRequest() {
        return sizeCreateRequest(0.1, 0.1, 0.9, 0.9, 120);
    }

    private AltarModelSizeCreateRequest sizeCreateRequest(
            double surfaceLeft, double surfaceTop, double surfaceRight, double surfaceBottom, int surfaceWidthCm) {
        return AltarModelSizeCreateRequest.builder()
                .label("127 x 61 cm")
                .widthCm(127)
                .depthCm(61)
                .backgroundImage("/files/bg.jpg")
                .surfaceLeft(surfaceLeft).surfaceTop(surfaceTop)
                .surfaceRight(surfaceRight).surfaceBottom(surfaceBottom)
                .surfaceWidthCm(surfaceWidthCm)
                .build();
    }

    @Test
    void getById_notFound_throws() {
        when(altarModelRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ALTAR_MODEL_NOT_FOUND);
    }

    @Test
    void getById_embedsSizes_orderedByPriority() {
        AltarModelEntity model = modelEntity();
        when(altarModelRepository.findById(1L)).thenReturn(Optional.of(model));
        when(altarModelMapper.toResponse(model)).thenReturn(AltarModelResponse.builder().id(1L).build());
        AltarModelSizeEntity size = sizeEntity(10L, model);
        when(altarModelSizeRepository.findByAltarModelIdOrderByPriorityAscIdAsc(1L))
                .thenReturn(List.of(size));
        when(altarModelSizeMapper.toResponse(size))
                .thenReturn(AltarModelSizeResponse.builder().id(10L).altarModelId(1L).build());

        AltarModelResponse response = service.getById(1L);

        assertThat(response.getSizes()).hasSize(1);
        assertThat(response.getSizes().get(0).getId()).isEqualTo(10L);
    }

    /**
     * Cascade-delete test: deleting a model with sizes must remove the sizes first (they carry
     * a not-null FK to the model), then the model itself.
     */
    @Test
    void delete_cascadesToSizes_thenDeletesModel() {
        AltarModelEntity model = modelEntity();
        when(altarModelRepository.findById(1L)).thenReturn(Optional.of(model));
        List<AltarModelSizeEntity> sizes = List.of(sizeEntity(10L, model), sizeEntity(11L, model));
        when(altarModelSizeRepository.findByAltarModelIdOrderByPriorityAscIdAsc(1L)).thenReturn(sizes);

        service.delete(1L);

        verify(altarModelSizeRepository).deleteAll(sizes);
        verify(altarModelRepository).delete(model);
    }

    @Test
    void createSize_modelNotFound_throws() {
        when(altarModelRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createSize(99L, validSizeCreateRequest()))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ALTAR_MODEL_NOT_FOUND);
    }

    @Test
    void createSize_valid_persists() {
        AltarModelEntity model = modelEntity();
        when(altarModelRepository.findById(1L)).thenReturn(Optional.of(model));
        when(altarModelSizeRepository.save(any(AltarModelSizeEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(altarModelSizeMapper.toResponse(any(AltarModelSizeEntity.class)))
                .thenReturn(AltarModelSizeResponse.builder().id(10L).build());

        AltarModelSizeResponse response = service.createSize(1L, validSizeCreateRequest());

        assertThat(response.getId()).isEqualTo(10L);
        verify(altarModelSizeRepository).save(any(AltarModelSizeEntity.class));
    }

    /** Malformed rect: surfaceLeft >= surfaceRight must be rejected before persisting. */
    @Test
    void createSize_leftNotLessThanRight_rejected() {
        AltarModelEntity model = modelEntity();
        when(altarModelRepository.findById(1L)).thenReturn(Optional.of(model));

        AltarModelSizeCreateRequest request = sizeCreateRequest(0.9, 0.1, 0.1, 0.9, 120);

        assertThatThrownBy(() -> service.createSize(1L, request))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.VALIDATION_ERROR);

        verify(altarModelSizeRepository, never()).save(any());
    }

    /** Malformed rect: surfaceTop >= surfaceBottom must be rejected before persisting. */
    @Test
    void createSize_topNotLessThanBottom_rejected() {
        AltarModelEntity model = modelEntity();
        when(altarModelRepository.findById(1L)).thenReturn(Optional.of(model));

        AltarModelSizeCreateRequest request = sizeCreateRequest(0.1, 0.9, 0.9, 0.1, 120);

        assertThatThrownBy(() -> service.createSize(1L, request))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.VALIDATION_ERROR);

        verify(altarModelSizeRepository, never()).save(any());
    }

    /** {@code surfaceWidthCm <= 0} must be rejected before persisting. */
    @Test
    void createSize_nonPositiveSurfaceWidthCm_rejected() {
        AltarModelEntity model = modelEntity();
        when(altarModelRepository.findById(1L)).thenReturn(Optional.of(model));

        AltarModelSizeCreateRequest request = sizeCreateRequest(0.1, 0.1, 0.9, 0.9, 0);

        assertThatThrownBy(() -> service.createSize(1L, request))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.VALIDATION_ERROR);

        verify(altarModelSizeRepository, never()).save(any());
    }

    /**
     * Partial update: patching only {@code surfaceRight} below the existing {@code surfaceLeft}
     * must still be caught — validation runs against the merged (existing + patched) rect.
     */
    @Test
    void updateSize_partialPatch_producingMalformedMergedRect_rejected() {
        AltarModelEntity model = modelEntity();
        when(altarModelRepository.findById(1L)).thenReturn(Optional.of(model));
        AltarModelSizeEntity existing = sizeEntity(10L, model); // surfaceLeft=0.1, surfaceRight=0.9
        when(altarModelSizeRepository.findById(10L)).thenReturn(Optional.of(existing));

        AltarModelSizeUpdateRequest request =
                AltarModelSizeUpdateRequest.builder().surfaceRight(0.05).build();

        assertThatThrownBy(() -> service.updateSize(1L, 10L, request))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.VALIDATION_ERROR);

        verify(altarModelSizeRepository, never()).save(any());
    }

    @Test
    void updateSize_sizeBelongsToDifferentModel_notFound() {
        AltarModelEntity model = modelEntity();
        when(altarModelRepository.findById(1L)).thenReturn(Optional.of(model));
        AltarModelEntity otherModel = modelEntity();
        otherModel.setId(2L);
        AltarModelSizeEntity existing = sizeEntity(10L, otherModel);
        when(altarModelSizeRepository.findById(10L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.updateSize(1L, 10L, AltarModelSizeUpdateRequest.builder().build()))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ALTAR_MODEL_SIZE_NOT_FOUND);
    }

    @Test
    void deleteSize_removesSize() {
        AltarModelEntity model = modelEntity();
        when(altarModelRepository.findById(1L)).thenReturn(Optional.of(model));
        AltarModelSizeEntity existing = sizeEntity(10L, model);
        when(altarModelSizeRepository.findById(10L)).thenReturn(Optional.of(existing));

        service.deleteSize(1L, 10L);

        verify(altarModelSizeRepository).delete(existing);
    }

    /** A size still referenced by a preset must not be deletable — block with 409, not a raw FK 500. */
    @Test
    void deleteSize_referencedByPreset_rejected() {
        AltarModelEntity model = modelEntity();
        when(altarModelRepository.findById(1L)).thenReturn(Optional.of(model));
        AltarModelSizeEntity existing = sizeEntity(10L, model);
        when(altarModelSizeRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(altarPresetRepository.existsByAltarModelSize_Id(10L)).thenReturn(true);

        assertThatThrownBy(() -> service.deleteSize(1L, 10L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ALTAR_MODEL_SIZE_REFERENCED);

        verify(altarModelSizeRepository, never()).delete(any(AltarModelSizeEntity.class));
    }

    /** Same guard, but the referencing row is a customer's saved design rather than a preset. */
    @Test
    void deleteSize_referencedByDesign_rejected() {
        AltarModelEntity model = modelEntity();
        when(altarModelRepository.findById(1L)).thenReturn(Optional.of(model));
        AltarModelSizeEntity existing = sizeEntity(10L, model);
        when(altarModelSizeRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(altarDesignRepository.existsByAltarModelSize_Id(10L)).thenReturn(true);

        assertThatThrownBy(() -> service.deleteSize(1L, 10L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ALTAR_MODEL_SIZE_REFERENCED);

        verify(altarModelSizeRepository, never()).delete(any(AltarModelSizeEntity.class));
    }

    /** Deleting a model whose size is still referenced must block before touching any row. */
    @Test
    void delete_sizeReferencedByPreset_rejected_noMutation() {
        AltarModelEntity model = modelEntity();
        when(altarModelRepository.findById(1L)).thenReturn(Optional.of(model));
        List<AltarModelSizeEntity> sizes = List.of(sizeEntity(10L, model));
        when(altarModelSizeRepository.findByAltarModelIdOrderByPriorityAscIdAsc(1L)).thenReturn(sizes);
        when(altarPresetRepository.existsByAltarModelSize_Id(10L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ALTAR_MODEL_SIZE_REFERENCED);

        verify(altarModelSizeRepository, never()).deleteAll(any());
        verify(altarModelRepository, never()).delete(any(AltarModelEntity.class));
    }
}
