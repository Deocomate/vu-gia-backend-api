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
import vn.springboot.dto.request.page.PageCreateRequest;
import vn.springboot.dto.request.page.PageSearchRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.page.PageDetailResponse;
import vn.springboot.entity.enums.ContentStatus;
import vn.springboot.entity.page.PageEntity;
import vn.springboot.mapper.PageMapper;
import vn.springboot.repository.PageRepository;
import vn.springboot.service.impl.PageServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PageServiceImplTest {

    @Mock
    private PageRepository pageRepository;

    @Mock
    private PageMapper pageMapper;

    @InjectMocks
    private PageServiceImpl service;

    private PageEntity entity(String key) {
        return PageEntity.builder().key(key).title("Title").status(ContentStatus.DRAFT).build();
    }

    private PageDetailResponse response(Long id, String key) {
        return PageDetailResponse.builder().id(id).key(key).title("Title").status(ContentStatus.DRAFT).build();
    }

    @Test
    void search_returnsPageResponse() {
        PageEntity e = entity("about-us");
        PageImpl<PageEntity> page = new PageImpl<>(List.of(e), PageRequest.of(0, 10), 1);
        when(pageRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(pageMapper.toResponse(e)).thenReturn(response(1L, "about-us"));

        PageResponse<PageDetailResponse> result = service.search(PageSearchRequest.builder().build());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getKey()).isEqualTo("about-us");
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getPageNumber()).isEqualTo(1);
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isTrue();
    }

    @Test
    void getByKey_notFound_throws() {
        when(pageRepository.findByKey("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByKey("missing"))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.PAGE_NOT_FOUND);
    }

    @Test
    void create_savesPage() {
        PageCreateRequest request = PageCreateRequest.builder()
                .key("about-us")
                .title("About us")
                .build();
        when(pageRepository.existsByKey("about-us")).thenReturn(false);
        when(pageRepository.save(any(PageEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pageMapper.toResponse(any(PageEntity.class))).thenAnswer(inv -> {
            PageEntity e = inv.getArgument(0);
            return response(1L, e.getKey());
        });

        PageDetailResponse result = service.create(request);

        assertThat(result.getKey()).isEqualTo("about-us");
        verify(pageRepository).save(any(PageEntity.class));
    }

    @Test
    void create_duplicateKey_throws() {
        PageCreateRequest request = PageCreateRequest.builder().key("dup").build();
        when(pageRepository.existsByKey("dup")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.PAGE_KEY_EXISTED);
        verify(pageRepository, never()).save(any());
    }
}
