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
import vn.springboot.dto.request.showroom.ShowroomCreateRequest;
import vn.springboot.dto.request.showroom.ShowroomSearchRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.showroom.ShowroomResponse;
import vn.springboot.entity.showroom.ShowroomEntity;
import vn.springboot.mapper.ShowroomMapper;
import vn.springboot.repository.ShowroomRepository;
import vn.springboot.service.impl.ShowroomServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShowroomServiceImplTest {

    @Mock
    private ShowroomRepository showroomRepository;

    @Mock
    private ShowroomMapper showroomMapper;

    @InjectMocks
    private ShowroomServiceImpl service;

    private ShowroomEntity entity(String name) {
        return ShowroomEntity.builder().name(name).address("123 Street").build();
    }

    private ShowroomResponse response(Long id, String name) {
        return ShowroomResponse.builder().id(id).name(name).address("123 Street").build();
    }

    @Test
    void search_returnsPageResponse() {
        ShowroomEntity e = entity("Hà Nội");
        PageImpl<ShowroomEntity> page =
                new PageImpl<>(List.of(e), PageRequest.of(0, 10), 1);
        when(showroomRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);
        when(showroomMapper.toResponse(e)).thenReturn(response(1L, "Hà Nội"));

        PageResponse<ShowroomResponse> result =
                service.search(ShowroomSearchRequest.builder().build());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Hà Nội");
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getPageNumber()).isEqualTo(1);
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isTrue();
    }

    @Test
    void getById_notFound_throws() {
        when(showroomRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.SHOWROOM_NOT_FOUND);
    }

    @Test
    void create_ok() {
        ShowroomCreateRequest request = ShowroomCreateRequest.builder()
                .name("Hà Nội")
                .address("123 Street")
                .build();
        when(showroomRepository.save(any(ShowroomEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(showroomMapper.toResponse(any(ShowroomEntity.class)))
                .thenAnswer(inv -> {
                    ShowroomEntity e = inv.getArgument(0);
                    return response(1L, e.getName());
                });

        ShowroomResponse result = service.create(request);

        assertThat(result.getName()).isEqualTo("Hà Nội");
        verify(showroomRepository).save(any(ShowroomEntity.class));
    }
}
