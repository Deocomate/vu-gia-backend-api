package vn.springboot.seed;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.springboot.entity.altar.AltarModelEntity;
import vn.springboot.entity.altar.AltarModelSizeEntity;
import vn.springboot.repository.AltarModelRepository;
import vn.springboot.repository.AltarModelSizeRepository;

import java.util.List;

/**
 * Seeds one altar model ("Bàn thờ gia tiên") and its size variants together — mirrors why
 * {@link ProductSeeder} seeds products and images together: sizes need the generated
 * {@code altar_model_id} from this seeder's own model insert.
 *
 * <p>Only one background image exists in the repo ({@code altar-preview.png}, per plan
 * decision D1), so every size shares it. Widths (127 / 153 / 175 cm) are real values taken
 * from the client's {@code SIZE_GUIDE_ROWS} table (rows 4-6's "altar" column upper bounds,
 * {@code altar-customizer-data.js}), enough to exercise cross-size re-mapping without
 * fabricating measurements. {@code surfaceLeft/Top/Right/Bottom} pick a sensible tabletop
 * region (roughly the lower-middle band of the background photo) shared by every size since
 * they're all rendered against the same background image.
 *
 * <p>No optional/nullable columns on either entity — every field is populated on every row.
 */
@Component
@RequiredArgsConstructor
public class AltarModelSeeder implements DomainSeeder {

    private static final String BACKGROUND_IMAGE = "assets/images/altar-customizer/altar-preview.png";

    private final AltarModelRepository altarModelRepository;
    private final AltarModelSizeRepository altarModelSizeRepository;

    @Override
    public boolean isEmpty() {
        return altarModelRepository.count() == 0;
    }

    @Override
    @Transactional
    public void reset() {
        altarModelSizeRepository.deleteAllInBatch();
        altarModelRepository.deleteAllInBatch();
    }

    @Override
    @Transactional
    public void seed() {
        AltarModelEntity model = altarModelRepository.save(AltarModelEntity.builder()
                .name("Bàn thờ gia tiên")
                .slug("ban-tho-gia-tien")
                .thumb(BACKGROUND_IMAGE)
                .description("Bàn thờ gia tiên gỗ tự nhiên, dáng truyền thống, phù hợp bài trí bộ đồ thờ gốm sứ Bát Tràng đầy đủ.")
                .priority(1)
                .isActive(true)
                .build());

        altarModelSizeRepository.saveAll(List.of(
                size(model, "127 cm", 127, 61, 1),
                size(model, "153 cm", 153, 70, 2),
                size(model, "175 cm", 175, 79, 3)));
    }

    private AltarModelSizeEntity size(AltarModelEntity model, String label, int widthCm, int depthCm, int priority) {
        return AltarModelSizeEntity.builder()
                .altarModel(model)
                .label(label)
                .widthCm(widthCm)
                .depthCm(depthCm)
                .backgroundImage(BACKGROUND_IMAGE)
                .surfaceLeft(0.12)
                .surfaceTop(0.58)
                .surfaceRight(0.88)
                .surfaceBottom(0.94)
                .surfaceWidthCm(widthCm)
                .priority(priority)
                .isActive(true)
                .build();
    }
}
