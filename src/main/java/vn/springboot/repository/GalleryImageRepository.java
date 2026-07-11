package vn.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.springboot.entity.gallery.GalleryImageEntity;

@Repository
public interface GalleryImageRepository
        extends JpaRepository<GalleryImageEntity, Long>, JpaSpecificationExecutor<GalleryImageEntity> {
}
