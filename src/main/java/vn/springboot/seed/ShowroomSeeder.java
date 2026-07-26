package vn.springboot.seed;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.springboot.entity.showroom.ShowroomEntity;
import vn.springboot.repository.ShowroomRepository;

import java.util.List;

/** Ported 1:1 from {@code V2__seed_db.sql} "SHOWROOMS" section (1 row, no FK). */
@Component
@RequiredArgsConstructor
public class ShowroomSeeder implements DomainSeeder {

    private final ShowroomRepository showroomRepository;

    @Override
    public boolean isEmpty() {
        return showroomRepository.count() == 0;
    }

    @Override
    @Transactional
    public void reset() {
        showroomRepository.deleteAllInBatch();
    }

    @Override
    @Transactional
    public void seed() {
        showroomRepository.save(ShowroomEntity.builder()
                .name("Showroom Bát Tràng")
                .phone("0966558808")
                .address("18 Giang Cao, Bát Tràng, Gia Lâm, Hà Nội")
                .mapEmbedUrl("https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3725.267812970921!2d105.9238384!3d20.9818818!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x3135af9e2a53ff33%3A0xe1adbfab1562e1ad!2zMTggR2lhbmcgQ2FvLCBCw6F0IFRyw6BuZywgR2lhIEzDom0sIEjDoCBO4buZaQ!5e0!3m2!1svi!2s!4v1718360000000!5m2!1svi!2s")
                .openingHours("08:00 - 18:00 (Thứ 2 - Chủ nhật)")
                .sortOrder(1)
                .isActive(true)
                .build());
    }
}
