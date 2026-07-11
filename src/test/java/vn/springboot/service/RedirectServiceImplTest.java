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
import vn.springboot.dto.request.redirect.RedirectCreateRequest;
import vn.springboot.dto.request.redirect.RedirectSearchRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.redirect.RedirectResponse;
import vn.springboot.entity.redirect.RedirectEntity;
import vn.springboot.mapper.RedirectMapper;
import vn.springboot.repository.RedirectRepository;
import vn.springboot.service.impl.RedirectServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedirectServiceImplTest {

    @Mock
    private RedirectRepository redirectRepository;

    @Mock
    private RedirectMapper redirectMapper;

    @InjectMocks
    private RedirectServiceImpl service;

    private RedirectEntity entity(String fromPath, String toPath, int statusCode) {
        return RedirectEntity.builder()
                .fromPath(fromPath)
                .toPath(toPath)
                .statusCode(statusCode)
                .build();
    }

    private RedirectResponse response(Long id, String fromPath, String toPath, int statusCode) {
        return RedirectResponse.builder()
                .id(id)
                .fromPath(fromPath)
                .toPath(toPath)
                .statusCode(statusCode)
                .build();
    }

    @Test
    void search_returnsPageResponse() {
        RedirectEntity e = entity("/old", "/new", 301);
        PageImpl<RedirectEntity> page =
                new PageImpl<>(List.of(e), PageRequest.of(0, 10), 1);
        when(redirectRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);
        when(redirectMapper.toResponse(e)).thenReturn(response(1L, "/old", "/new", 301));

        PageResponse<RedirectResponse> result =
                service.search(RedirectSearchRequest.builder().build());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getFromPath()).isEqualTo("/old");
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getPageNumber()).isEqualTo(1);
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isTrue();
    }

    @Test
    void getById_notFound_throws() {
        when(redirectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.REDIRECT_NOT_FOUND);
    }

    @Test
    void create_fromPathConflict_throws() {
        RedirectCreateRequest request = RedirectCreateRequest.builder()
                .fromPath("/old")
                .toPath("/new")
                .build();
        when(redirectRepository.existsByFromPath("/old")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.REDIRECT_FROM_PATH_EXISTED);

        verify(redirectRepository, never()).save(any());
    }

    @Test
    void create_defaultsStatusCodeTo301_whenNull() {
        RedirectCreateRequest request = RedirectCreateRequest.builder()
                .fromPath("/old")
                .toPath("/new")
                .build();
        when(redirectRepository.existsByFromPath("/old")).thenReturn(false);
        when(redirectRepository.save(any(RedirectEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(redirectMapper.toResponse(any(RedirectEntity.class)))
                .thenAnswer(inv -> {
                    RedirectEntity e = inv.getArgument(0);
                    return response(1L, e.getFromPath(), e.getToPath(), e.getStatusCode());
                });

        RedirectResponse result = service.create(request);

        assertThat(result.getStatusCode()).isEqualTo(301);
        verify(redirectRepository).save(any(RedirectEntity.class));
    }
}
