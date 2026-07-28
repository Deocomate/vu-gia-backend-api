package vn.springboot.seed;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.springboot.entity.altar.AltarItemGroupEntity;
import vn.springboot.repository.AltarItemGroupRepository;

import java.util.List;

/**
 * Seeds the altar-accessory "group" catalog the customizer palette tabs are built from
 * (Phase 6 of the altar-customizer build). Six rows: the five real-world groups the
 * original requirement named (all {@code renderOnAltar=true} — placeable on the canvas
 * surface, subject to a real {@link vn.springboot.entity.altar.AltarPlacementEntity}
 * existing for the specific product image) plus one catch-all "Phụ kiện đi kèm"
 * ({@code renderOnAltar=false}) for items that only ever appear as a summary/quantity
 * line, never dragged onto the altar (see {@link AltarPresetSeeder}'s accessory items).
 *
 * <p>No optional/nullable columns on this entity — every field is populated on every row.
 */
@Component
@RequiredArgsConstructor
public class AltarItemGroupSeeder implements DomainSeeder {

    private final AltarItemGroupRepository altarItemGroupRepository;

    @Override
    public boolean isEmpty() {
        return altarItemGroupRepository.count() == 0;
    }

    @Override
    @Transactional
    public void reset() {
        altarItemGroupRepository.deleteAllInBatch();
    }

    @Override
    @Transactional
    public void seed() {
        altarItemGroupRepository.saveAll(List.of(
                AltarItemGroupEntity.builder()
                        .name("Bộ tam sự - ngũ sự")
                        .slug("bo-tam-su-ngu-su")
                        .thumb("assets/images/altar-customizer/altar-preview.png")
                        .renderOnAltar(true)
                        .priority(1)
                        .isActive(true)
                        .build(),
                AltarItemGroupEntity.builder()
                        .name("Bát hương & phụ kiện")
                        .slug("bat-huong-phu-kien")
                        .thumb("assets/images/altar-customizer/bat-huong-1.png")
                        .renderOnAltar(true)
                        .priority(2)
                        .isActive(true)
                        .build(),
                AltarItemGroupEntity.builder()
                        .name("Lọ hoa & phụ kiện")
                        .slug("lo-hoa-phu-kien")
                        .thumb("assets/images/altar-customizer/similar-product-1.jpg")
                        .renderOnAltar(true)
                        .priority(3)
                        .isActive(true)
                        .build(),
                AltarItemGroupEntity.builder()
                        .name("Phụ kiện thờ")
                        .slug("phu-kien-tho")
                        .thumb("assets/images/altar-customizer/similar-product-2.jpg")
                        .renderOnAltar(true)
                        .priority(4)
                        .isActive(true)
                        .build(),
                AltarItemGroupEntity.builder()
                        .name("Thần tài - Thổ địa")
                        .slug("than-tai-tho-dia")
                        .thumb("assets/images/altar-customizer/similar-product-3.jpg")
                        .renderOnAltar(true)
                        .priority(5)
                        .isActive(true)
                        .build(),
                AltarItemGroupEntity.builder()
                        .name("Phụ kiện đi kèm")
                        .slug("phu-kien-di-kem")
                        .thumb("assets/images/altar-customizer/accessories-sprite.png")
                        .renderOnAltar(false)
                        .priority(6)
                        .isActive(true)
                        .build()));
    }
}
