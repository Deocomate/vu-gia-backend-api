package vn.springboot.seed;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.springboot.entity.altar.AltarItemGroupEntity;
import vn.springboot.repository.AltarItemGroupRepository;

import java.util.List;

/**
 * Seeds the altar-product "group" catalog the customizer palette tabs are built from. Eight
 * rows: the 7 ceramic families the real 25-product catalog naturally clusters into (all
 * {@code renderOnAltar=true} — placeable on the canvas surface, subject to a real
 * {@link vn.springboot.entity.altar.AltarPlacementEntity} existing for the specific product
 * image) plus one catch-all "Phụ kiện đi kèm" ({@code renderOnAltar=false}) for the 3
 * accessories that only ever appear as a summary/quantity line, never dragged onto the altar
 * (see {@link AltarPresetSeeder}'s accessory items). "Thần tài - Thổ địa" and the other
 * placeholder groups from the 9-product catalog are gone: no source photo is a Thần tài/Thổ
 * địa item, and a tab that filters to zero products is worse than no tab.
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
                        .name("Bát hương")
                        .slug("bat-huong")
                        .thumb("assets/images/altar-customizer/products/bat-huong-men-lam-ve-rong-h20/01.png")
                        .renderOnAltar(true)
                        .priority(1)
                        .isActive(true)
                        .build(),
                AltarItemGroupEntity.builder()
                        .name("Lọ hoa")
                        .slug("lo-hoa")
                        .thumb("assets/images/altar-customizer/products/lo-hoa-men-lam-h30/01.png")
                        .renderOnAltar(true)
                        .priority(2)
                        .isActive(true)
                        .build(),
                AltarItemGroupEntity.builder()
                        .name("Chóe thờ")
                        .slug("choe-tho")
                        .thumb("assets/images/altar-customizer/products/choe-tho-men-lam-h19/01.png")
                        .renderOnAltar(true)
                        .priority(3)
                        .isActive(true)
                        .build(),
                AltarItemGroupEntity.builder()
                        .name("Bát thờ & bát sâm")
                        .slug("bat-tho-bat-sam")
                        .thumb("assets/images/altar-customizer/products/bat-sam-men-lam-ve-rong-phuong/01.png")
                        .renderOnAltar(true)
                        .priority(4)
                        .isActive(true)
                        .build(),
                AltarItemGroupEntity.builder()
                        .name("Nậm rượu & kỷ chén")
                        .slug("nam-ruou-ky-chen")
                        .thumb("assets/images/altar-customizer/products/ky-5-chen-men-lam-de-rong/01.png")
                        .renderOnAltar(true)
                        .priority(5)
                        .isActive(true)
                        .build(),
                AltarItemGroupEntity.builder()
                        .name("Đèn thờ & chân nến")
                        .slug("den-tho-chan-nen")
                        .thumb("assets/images/altar-customizer/products/doi-hac-tho-men-lam-ve-vang/01.png")
                        .renderOnAltar(true)
                        .priority(6)
                        .isActive(true)
                        .build(),
                AltarItemGroupEntity.builder()
                        .name("Ống hương & mâm bồng")
                        .slug("ong-huong-mam-bong")
                        .thumb("assets/images/altar-customizer/products/ong-huong-men-lam-h31/01.png")
                        .renderOnAltar(true)
                        .priority(7)
                        .isActive(true)
                        .build(),
                AltarItemGroupEntity.builder()
                        .name("Phụ kiện đi kèm")
                        .slug("phu-kien-di-kem")
                        .thumb("assets/images/altar-customizer/products/choe-tho-men-lam-h14/01.png")
                        .renderOnAltar(false)
                        .priority(8)
                        .isActive(true)
                        .build()));
    }
}
