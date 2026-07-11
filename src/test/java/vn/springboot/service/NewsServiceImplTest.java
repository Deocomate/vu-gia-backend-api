package vn.springboot.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import vn.springboot.common.exception.AppException;
import vn.springboot.common.exception.ErrorCode;
import vn.springboot.dto.request.news.NewsCreateRequest;
import vn.springboot.dto.request.news.NewsSearchRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.news.NewsResponse;
import vn.springboot.entity.enums.ContentStatus;
import vn.springboot.entity.news.NewsCategoryEntity;
import vn.springboot.entity.news.NewsEntity;
import vn.springboot.mapper.NewsMapper;
import vn.springboot.repository.NewsCategoryRepository;
import vn.springboot.repository.NewsRepository;
import vn.springboot.service.impl.NewsServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsServiceImplTest {

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private NewsCategoryRepository newsCategoryRepository;

    @Mock
    private NewsMapper newsMapper;

    @InjectMocks
    private NewsServiceImpl service;

    private NewsEntity newsEntity(String title, String slug) {
        return NewsEntity.builder()
                .title(title)
                .thumb("http://img/x.png")
                .shortContent("short")
                .des("{}")
                .slug(slug)
                .build();
    }

    private NewsResponse response(Long id, String title, String slug) {
        return NewsResponse.builder().id(id).title(title).slug(slug).build();
    }

    private NewsCreateRequest.NewsCreateRequestBuilder validCreate() {
        return NewsCreateRequest.builder()
                .title("Hello World")
                .thumb("http://img/x.png")
                .shortContent("short")
                .des("{}")
                .newsCategoryId(5L);
    }

    @Test
    void search_returnsPageResponse() {
        NewsEntity e = newsEntity("Hello World", "hello-world");
        PageImpl<NewsEntity> page = new PageImpl<>(List.of(e), PageRequest.of(0, 10), 1);
        when(newsRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(newsMapper.toResponse(e)).thenReturn(response(1L, "Hello World", "hello-world"));

        PageResponse<NewsResponse> result = service.search(NewsSearchRequest.builder().build());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getSlug()).isEqualTo("hello-world");
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getPageNumber()).isEqualTo(1);
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isTrue();
    }

    @Test
    void getById_notFound_throws() {
        when(newsRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NEWS_NOT_FOUND);
    }

    @Test
    void create_categoryNotFound_throws() {
        when(newsCategoryRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(validCreate().build()))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NEWS_CATEGORY_NOT_FOUND);

        verify(newsRepository, never()).save(any());
    }

    @Test
    void create_clientSlugConflict_throws() {
        when(newsCategoryRepository.findById(5L))
                .thenReturn(Optional.of(new NewsCategoryEntity()));
        when(newsRepository.existsBySlug("hello-world")).thenReturn(true);

        assertThatThrownBy(() -> service.create(validCreate().slug("hello-world").build()))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NEWS_SLUG_EXISTED);

        verify(newsRepository, never()).save(any());
    }

    @Test
    void create_published_setsPublishedAt() {
        when(newsCategoryRepository.findById(5L))
                .thenReturn(Optional.of(new NewsCategoryEntity()));
        when(newsRepository.existsBySlug(anyString())).thenReturn(false);
        when(newsRepository.save(any(NewsEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(newsMapper.toResponse(any(NewsEntity.class))).thenReturn(response(1L, "Hello World", "hello-world"));

        service.create(validCreate().status(ContentStatus.PUBLISHED).build());

        ArgumentCaptor<NewsEntity> captor = ArgumentCaptor.forClass(NewsEntity.class);
        verify(newsRepository).save(captor.capture());
        NewsEntity saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(ContentStatus.PUBLISHED);
        assertThat(saved.getPublishedAt()).isNotNull();
    }

    @Test
    void create_draft_leavesPublishedAtNull() {
        when(newsCategoryRepository.findById(5L))
                .thenReturn(Optional.of(new NewsCategoryEntity()));
        when(newsRepository.existsBySlug(anyString())).thenReturn(false);
        when(newsRepository.save(any(NewsEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(newsMapper.toResponse(any(NewsEntity.class))).thenReturn(response(1L, "Hello World", "hello-world"));

        service.create(validCreate().build());

        ArgumentCaptor<NewsEntity> captor = ArgumentCaptor.forClass(NewsEntity.class);
        verify(newsRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(ContentStatus.DRAFT);
        assertThat(captor.getValue().getPublishedAt()).isNull();
    }

    @Test
    void getBySlug_incrementsViewCount_andReturnsBumpedCount() {
        NewsEntity e = newsEntity("Hello World", "hello-world");
        e.setViewCount(41);
        when(newsRepository.findBySlug("hello-world")).thenReturn(Optional.of(e));
        when(newsMapper.toResponse(e)).thenReturn(response(1L, "Hello World", "hello-world"));

        NewsResponse result = service.getBySlug("hello-world");

        verify(newsRepository).incrementViewCount(eq(e.getId()));
        assertThat(result.getViewCount()).isEqualTo(42);
    }
}
