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
import vn.springboot.dto.request.news.NewsCategoryCreateRequest;
import vn.springboot.dto.request.news.NewsCategorySearchRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.news.NewsCategoryResponse;
import vn.springboot.entity.news.NewsCategoryEntity;
import vn.springboot.mapper.NewsCategoryMapper;
import vn.springboot.repository.NewsCategoryRepository;
import vn.springboot.repository.NewsRepository;
import vn.springboot.service.impl.NewsCategoryServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsCategoryServiceImplTest {

    @Mock
    private NewsCategoryRepository newsCategoryRepository;

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private NewsCategoryMapper newsCategoryMapper;

    @InjectMocks
    private NewsCategoryServiceImpl service;

    private NewsCategoryEntity entity(String name, String slug) {
        return NewsCategoryEntity.builder().name(name).slug(slug).build();
    }

    private NewsCategoryResponse response(Long id, String name, String slug) {
        return NewsCategoryResponse.builder().id(id).name(name).slug(slug).build();
    }

    @Test
    void search_returnsPageResponse() {
        NewsCategoryEntity e = entity("Thời sự", "thoi-su");
        PageImpl<NewsCategoryEntity> page =
                new PageImpl<>(List.of(e), PageRequest.of(0, 10), 1);
        when(newsCategoryRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);
        when(newsCategoryMapper.toResponse(e)).thenReturn(response(1L, "Thời sự", "thoi-su"));

        PageResponse<NewsCategoryResponse> result =
                service.search(NewsCategorySearchRequest.builder().build());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getSlug()).isEqualTo("thoi-su");
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getPageNumber()).isEqualTo(1);
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isTrue();
    }

    @Test
    void getById_notFound_throws() {
        when(newsCategoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NEWS_CATEGORY_NOT_FOUND);
    }

    @Test
    void getBySlug_returnsResponse() {
        NewsCategoryEntity e = entity("Thời sự", "thoi-su");
        when(newsCategoryRepository.findBySlug("thoi-su")).thenReturn(Optional.of(e));
        when(newsCategoryMapper.toResponse(e)).thenReturn(response(1L, "Thời sự", "thoi-su"));

        NewsCategoryResponse result = service.getBySlug("thoi-su");

        assertThat(result.getSlug()).isEqualTo("thoi-su");
    }

    @Test
    void getBySlug_notFound_throws() {
        when(newsCategoryRepository.findBySlug("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getBySlug("missing"))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NEWS_CATEGORY_NOT_FOUND);
    }

    @Test
    void create_generatesSlug_whenBlank() {
        NewsCategoryCreateRequest request = NewsCategoryCreateRequest.builder()
                .name("Thời sự")
                .build();
        when(newsCategoryRepository.existsBySlug(anyString())).thenReturn(false);
        when(newsCategoryRepository.save(any(NewsCategoryEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(newsCategoryMapper.toResponse(any(NewsCategoryEntity.class)))
                .thenAnswer(inv -> {
                    NewsCategoryEntity e = inv.getArgument(0);
                    return response(1L, e.getName(), e.getSlug());
                });

        NewsCategoryResponse result = service.create(request);

        assertThat(result.getSlug()).isEqualTo("thoi-su");
        verify(newsCategoryRepository).save(any(NewsCategoryEntity.class));
    }

    @Test
    void create_clientSlugConflict_throws() {
        NewsCategoryCreateRequest request = NewsCategoryCreateRequest.builder()
                .name("Thời sự")
                .slug("thoi-su")
                .build();
        when(newsCategoryRepository.existsBySlug("thoi-su")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NEWS_CATEGORY_SLUG_EXISTED);

        verify(newsCategoryRepository, never()).save(any());
    }

    @Test
    void delete_withNews_throws() {
        NewsCategoryEntity e = entity("Thời sự", "thoi-su");
        when(newsCategoryRepository.findById(1L)).thenReturn(Optional.of(e));
        when(newsRepository.existsByNewsCategoryId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NEWS_CATEGORY_HAS_NEWS);

        verify(newsCategoryRepository, never()).delete(any(NewsCategoryEntity.class));
    }
}
