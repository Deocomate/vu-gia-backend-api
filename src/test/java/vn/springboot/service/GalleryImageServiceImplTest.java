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
import vn.springboot.dto.request.gallery.GalleryImageCreateRequest;
import vn.springboot.dto.request.gallery.GalleryImageSearchRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.gallery.GalleryImageResponse;
import vn.springboot.entity.gallery.GalleryImageEntity;
import vn.springboot.mapper.GalleryImageMapper;
import vn.springboot.repository.GalleryImageRepository;
import vn.springboot.service.impl.GalleryImageServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GalleryImageServiceImplTest {

    @Mock
    private GalleryImageRepository galleryImageRepository;

    @Mock
    private GalleryImageMapper galleryImageMapper;

    @InjectMocks
    private GalleryImageServiceImpl service;

    private GalleryImageEntity entity(String imageUrl, String title) {
        return GalleryImageEntity.builder().imageUrl(imageUrl).title(title).build();
    }

    private GalleryImageResponse response(Long id, String imageUrl, String title) {
        return GalleryImageResponse.builder().id(id).imageUrl(imageUrl).title(title).build();
    }

    @Test
    void search_returnsPageResponse() {
        GalleryImageEntity e = entity("http://img/1.png", "Banner");
        PageImpl<GalleryImageEntity> page =
                new PageImpl<>(List.of(e), PageRequest.of(0, 10), 1);
        when(galleryImageRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);
        when(galleryImageMapper.toResponse(e)).thenReturn(response(1L, "http://img/1.png", "Banner"));

        PageResponse<GalleryImageResponse> result =
                service.search(GalleryImageSearchRequest.builder().build());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Banner");
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getPageNumber()).isEqualTo(1);
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isTrue();
    }

    @Test
    void getById_notFound_throws() {
        when(galleryImageRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.GALLERY_IMAGE_NOT_FOUND);
    }

    @Test
    void create_ok_appliesDefaults() {
        GalleryImageCreateRequest request = GalleryImageCreateRequest.builder()
                .imageUrl("http://img/1.png")
                .title("Banner")
                .build();
        when(galleryImageRepository.save(any(GalleryImageEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(galleryImageMapper.toResponse(any(GalleryImageEntity.class)))
                .thenAnswer(inv -> {
                    GalleryImageEntity e = inv.getArgument(0);
                    return response(1L, e.getImageUrl(), e.getTitle());
                });

        GalleryImageResponse result = service.create(request);

        assertThat(result.getImageUrl()).isEqualTo("http://img/1.png");
        assertThat(result.getTitle()).isEqualTo("Banner");
        verify(galleryImageRepository).save(any(GalleryImageEntity.class));
    }
}
