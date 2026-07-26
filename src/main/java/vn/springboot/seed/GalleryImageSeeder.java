package vn.springboot.seed;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.springboot.entity.gallery.GalleryImageEntity;
import vn.springboot.repository.GalleryImageRepository;

import java.util.List;

/** Ported 1:1 from {@code V2__seed_db.sql} "GALLERY IMAGES" section (5 rows, no FK). */
@Component
@RequiredArgsConstructor
public class GalleryImageSeeder implements DomainSeeder {

    private static final String CATEGORY = "Hình ảnh của khách hàng";

    private final GalleryImageRepository galleryImageRepository;

    @Override
    public boolean isEmpty() {
        return galleryImageRepository.count() == 0;
    }

    @Override
    @Transactional
    public void reset() {
        galleryImageRepository.deleteAllInBatch();
    }

    @Override
    @Transactional
    public void seed() {
        galleryImageRepository.saveAll(List.of(
                image("assets/images/gallery/gallery-1.jpg", "Kệ phơi sản phẩm gốm mộc tại xưởng chế tác", 1),
                image("assets/images/gallery/gallery-2.jpg", "Các chi tiết ấm chén trà được nghệ nhân tạo hình hoàn thiện", 2),
                image("assets/images/gallery/gallery-3.jpg", "Nghệ nhân gốm khéo léo tạo dáng bình trên bàn xoay truyền thống", 3),
                image("assets/images/gallery/gallery-4.jpg", "Hàng gốm mộc xếp đều tăm tắp chờ công đoạn tráng men", 4),
                image("assets/images/gallery/gallery-5.jpg", "Các tác phẩm bình phong thủy men rạn độc bản hoàn thiện", 5)));
    }

    private GalleryImageEntity image(String imageUrl, String title, int sortOrder) {
        return GalleryImageEntity.builder()
                .imageUrl(imageUrl)
                .title(title)
                .category(CATEGORY)
                .sortOrder(sortOrder)
                .isActive(true)
                .build();
    }
}
