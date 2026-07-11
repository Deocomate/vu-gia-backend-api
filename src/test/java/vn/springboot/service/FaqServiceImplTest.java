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
import vn.springboot.dto.request.faq.FaqCreateRequest;
import vn.springboot.dto.request.faq.FaqSearchRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.faq.FaqResponse;
import vn.springboot.entity.faq.FaqEntity;
import vn.springboot.mapper.FaqMapper;
import vn.springboot.repository.FaqRepository;
import vn.springboot.service.impl.FaqServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FaqServiceImplTest {

    @Mock
    private FaqRepository faqRepository;

    @Mock
    private FaqMapper faqMapper;

    @InjectMocks
    private FaqServiceImpl service;

    private FaqEntity entity(String question) {
        return FaqEntity.builder().question(question).answer("answer").build();
    }

    private FaqResponse response(Long id, String question) {
        return FaqResponse.builder().id(id).question(question).answer("answer").build();
    }

    @Test
    void search_returnsPageResponse() {
        FaqEntity e = entity("How to return?");
        PageImpl<FaqEntity> page =
                new PageImpl<>(List.of(e), PageRequest.of(0, 10), 1);
        when(faqRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);
        when(faqMapper.toResponse(e)).thenReturn(response(1L, "How to return?"));

        PageResponse<FaqResponse> result =
                service.search(FaqSearchRequest.builder().build());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getQuestion()).isEqualTo("How to return?");
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getPageNumber()).isEqualTo(1);
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isTrue();
    }

    @Test
    void getById_notFound_throws() {
        when(faqRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.FAQ_NOT_FOUND);
    }

    @Test
    void create_savesFaq() {
        FaqCreateRequest request = FaqCreateRequest.builder()
                .question("How to return?")
                .answer("Contact support.")
                .build();
        when(faqRepository.save(any(FaqEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(faqMapper.toResponse(any(FaqEntity.class)))
                .thenAnswer(inv -> {
                    FaqEntity e = inv.getArgument(0);
                    return response(1L, e.getQuestion());
                });

        FaqResponse result = service.create(request);

        assertThat(result.getQuestion()).isEqualTo("How to return?");
        verify(faqRepository).save(any(FaqEntity.class));
    }
}
