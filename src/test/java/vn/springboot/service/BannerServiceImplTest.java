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
import vn.springboot.dto.request.banner.BannerCreateRequest;
import vn.springboot.dto.request.banner.BannerSearchRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.banner.BannerResponse;
import vn.springboot.entity.banner.BannerEntity;
import vn.springboot.entity.enums.BannerPosition;
import vn.springboot.mapper.BannerMapper;
import vn.springboot.repository.BannerRepository;
import vn.springboot.service.impl.BannerServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BannerServiceImplTest {

    @Mock
    private BannerRepository bannerRepository;

    @Mock
    private BannerMapper bannerMapper;

    @InjectMocks
    private BannerServiceImpl service;

    private BannerEntity entity(String title) {
        return BannerEntity.builder()
                .title(title)
                .imageUrl("http://img/x.png")
                .position(BannerPosition.HOME_HERO)
                .build();
    }

    private BannerResponse response(Long id, String title) {
        return BannerResponse.builder()
                .id(id)
                .title(title)
                .imageUrl("http://img/x.png")
                .position(BannerPosition.HOME_HERO)
                .build();
    }

    @Test
    void search_returnsPageResponse() {
        BannerEntity e = entity("Hero");
        PageImpl<BannerEntity> page =
                new PageImpl<>(List.of(e), PageRequest.of(0, 10), 1);
        when(bannerRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);
        when(bannerMapper.toResponse(e)).thenReturn(response(1L, "Hero"));

        PageResponse<BannerResponse> result =
                service.search(BannerSearchRequest.builder().build());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Hero");
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getPageNumber()).isEqualTo(1);
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isTrue();
    }

    @Test
    void getById_notFound_throws() {
        when(bannerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.BANNER_NOT_FOUND);
    }

    @Test
    void create_defaultsSortOrderAndActive() {
        BannerCreateRequest request = BannerCreateRequest.builder()
                .title("Hero")
                .imageUrl("http://img/x.png")
                .position(BannerPosition.HOME_HERO)
                .build();
        when(bannerRepository.save(any(BannerEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(bannerMapper.toResponse(any(BannerEntity.class)))
                .thenAnswer(inv -> {
                    BannerEntity e = inv.getArgument(0);
                    BannerResponse r = response(1L, e.getTitle());
                    r.setSortOrder(e.getSortOrder());
                    r.setActive(e.isActive());
                    return r;
                });

        BannerResponse result = service.create(request);

        assertThat(result.getSortOrder()).isEqualTo(0);
        assertThat(result.isActive()).isTrue();
        verify(bannerRepository).save(any(BannerEntity.class));
    }
}
