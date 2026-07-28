package vn.springboot.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import vn.springboot.dto.response.altar.AltarItemGroupResponse;
import vn.springboot.dto.response.altar.AltarModelResponse;
import vn.springboot.dto.response.altar.AltarModelSizeResponse;
import vn.springboot.dto.response.altar.AltarPlacementResponse;
import vn.springboot.dto.response.altar.AltarStyleResponse;
import vn.springboot.entity.altar.AltarItemGroupEntity;
import vn.springboot.entity.altar.AltarModelEntity;
import vn.springboot.entity.altar.AltarModelSizeEntity;
import vn.springboot.entity.altar.AltarPlacementEntity;
import vn.springboot.entity.altar.AltarStyleEntity;
import vn.springboot.entity.product.ProductImageEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies every altar-catalog mapper serializes its {@code isActive}/{@code renderOnAltar}
 * booleans as real booleans, not silently {@code false}/dropped. Seven prior instances of this
 * exact bug (primitive {@code isActive} field -> Lombok getter property "active" -> MapStruct
 * by-name mismatch with the wrapper-{@code Boolean} target) exist in repo history; this is the
 * regression guard mandated for the altar-catalog domain (Phase 1 of the altar-customizer plan).
 */
class AltarMapperTest {

    @Test
    void altarItemGroupMapper_mapsIsActiveAndRenderOnAltar_asRealBooleans() {
        AltarItemGroupMapper mapper = Mappers.getMapper(AltarItemGroupMapper.class);
        AltarItemGroupEntity entity = AltarItemGroupEntity.builder()
                .name("Bộ tam sự")
                .slug("bo-tam-su")
                .thumb("/files/thumb.jpg")
                .renderOnAltar(true)
                .isActive(true)
                .build();

        AltarItemGroupResponse response = mapper.toResponse(entity);

        assertThat(response.getIsActive()).isTrue();
        assertThat(response.getRenderOnAltar()).isTrue();

        entity.setActive(false);
        entity.setRenderOnAltar(false);
        AltarItemGroupResponse falseResponse = mapper.toResponse(entity);
        assertThat(falseResponse.getIsActive()).isFalse();
        assertThat(falseResponse.getRenderOnAltar()).isFalse();
    }

    @Test
    void altarStyleMapper_mapsIsActive_asRealBoolean() {
        AltarStyleMapper mapper = Mappers.getMapper(AltarStyleMapper.class);
        AltarStyleEntity entity = AltarStyleEntity.builder()
                .name("Men lam")
                .slug("men-lam")
                .thumb("/files/thumb.jpg")
                .description("desc")
                .isActive(true)
                .build();

        assertThat(mapper.toResponse(entity).getIsActive()).isTrue();

        entity.setActive(false);
        assertThat(mapper.toResponse(entity).getIsActive()).isFalse();
    }

    @Test
    void altarModelMapper_mapsIsActive_asRealBoolean() {
        AltarModelMapper mapper = Mappers.getMapper(AltarModelMapper.class);
        AltarModelEntity entity = AltarModelEntity.builder()
                .name("Bàn thờ gia tiên")
                .slug("ban-tho-gia-tien")
                .thumb("/files/thumb.jpg")
                .description("desc")
                .isActive(true)
                .build();

        AltarModelResponse response = mapper.toResponse(entity);
        assertThat(response.getIsActive()).isTrue();
        // sizes is deliberately ignored by the mapper; the service populates it separately.
        assertThat(response.getSizes()).isNull();

        entity.setActive(false);
        assertThat(mapper.toResponse(entity).getIsActive()).isFalse();
    }

    @Test
    void altarModelSizeMapper_mapsIsActive_asRealBoolean_andAltarModelId() {
        AltarModelSizeMapper mapper = Mappers.getMapper(AltarModelSizeMapper.class);
        AltarModelEntity model = AltarModelEntity.builder().name("m").slug("m").thumb("t").description("d").build();
        model.setId(42L);
        AltarModelSizeEntity entity = AltarModelSizeEntity.builder()
                .altarModel(model)
                .label("127 x 61 cm")
                .widthCm(127)
                .depthCm(61)
                .backgroundImage("/files/bg.jpg")
                .surfaceLeft(0.1)
                .surfaceTop(0.2)
                .surfaceRight(0.9)
                .surfaceBottom(0.95)
                .surfaceWidthCm(120)
                .isActive(true)
                .build();

        AltarModelSizeResponse response = mapper.toResponse(entity);

        assertThat(response.getIsActive()).isTrue();
        assertThat(response.getAltarModelId()).isEqualTo(42L);

        entity.setActive(false);
        assertThat(mapper.toResponse(entity).getIsActive()).isFalse();
    }

    @Test
    void altarPlacementMapper_mapsFlippableAsRealBoolean_andProductImageId() {
        AltarPlacementMapper mapper = Mappers.getMapper(AltarPlacementMapper.class);
        ProductImageEntity image = ProductImageEntity.builder().url("/files/img.png").build();
        image.setId(7L);
        AltarPlacementEntity entity = AltarPlacementEntity.builder()
                .productImage(image)
                .overlayImage("/files/overlay.png")
                .defaultX(0.5)
                .defaultY(0.8)
                .widthCm(30)
                .scaleAdjust(1.0)
                .flippable(true)
                .build();

        AltarPlacementResponse response = mapper.toResponse(entity);
        assertThat(response.getProductImageId()).isEqualTo(7L);
        assertThat(response.getFlippable()).isTrue();

        entity.setFlippable(false);
        assertThat(mapper.toResponse(entity).getFlippable()).isFalse();
    }
}
