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
import vn.springboot.dto.request.newsletter.NewsletterSubscribeRequest;
import vn.springboot.dto.request.newsletter.NewsletterSubscriberSearchRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.newsletter.NewsletterSubscriberResponse;
import vn.springboot.entity.newsletter.NewsletterSubscriberEntity;
import vn.springboot.mapper.NewsletterSubscriberMapper;
import vn.springboot.repository.NewsletterSubscriberRepository;
import vn.springboot.service.impl.NewsletterSubscriberServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsletterSubscriberServiceImplTest {

    @Mock
    private NewsletterSubscriberRepository subscriberRepository;

    @Mock
    private NewsletterSubscriberMapper subscriberMapper;

    @InjectMocks
    private NewsletterSubscriberServiceImpl service;

    private NewsletterSubscriberEntity entity(String email) {
        return NewsletterSubscriberEntity.builder().email(email).isActive(true).build();
    }

    private NewsletterSubscriberResponse response(Long id, String email) {
        return NewsletterSubscriberResponse.builder().id(id).email(email).isActive(true).build();
    }

    @Test
    void search_returnsPageResponse() {
        NewsletterSubscriberEntity e = entity("a@b.com");
        PageImpl<NewsletterSubscriberEntity> page =
                new PageImpl<>(List.of(e), PageRequest.of(0, 10), 1);
        when(subscriberRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);
        when(subscriberMapper.toResponse(e)).thenReturn(response(1L, "a@b.com"));

        PageResponse<NewsletterSubscriberResponse> result =
                service.search(NewsletterSubscriberSearchRequest.builder().build());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("a@b.com");
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getPageNumber()).isEqualTo(1);
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isTrue();
    }

    @Test
    void getById_notFound_throws() {
        when(subscriberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NEWSLETTER_SUBSCRIBER_NOT_FOUND);
    }

    @Test
    void subscribe_savesSubscriber() {
        NewsletterSubscribeRequest request = NewsletterSubscribeRequest.builder()
                .email("new@b.com")
                .build();
        when(subscriberRepository.existsByEmail("new@b.com")).thenReturn(false);
        when(subscriberRepository.save(any(NewsletterSubscriberEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(subscriberMapper.toResponse(any(NewsletterSubscriberEntity.class)))
                .thenAnswer(inv -> {
                    NewsletterSubscriberEntity e = inv.getArgument(0);
                    return response(1L, e.getEmail());
                });

        NewsletterSubscriberResponse result = service.subscribe(request);

        assertThat(result.getEmail()).isEqualTo("new@b.com");
        verify(subscriberRepository).save(any(NewsletterSubscriberEntity.class));
    }

    @Test
    void subscribe_duplicateEmail_throws() {
        NewsletterSubscribeRequest request = NewsletterSubscribeRequest.builder()
                .email("dup@b.com")
                .build();
        when(subscriberRepository.existsByEmail("dup@b.com")).thenReturn(true);

        assertThatThrownBy(() -> service.subscribe(request))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NEWSLETTER_EMAIL_EXISTED);
        verify(subscriberRepository, never()).save(any());
    }
}
