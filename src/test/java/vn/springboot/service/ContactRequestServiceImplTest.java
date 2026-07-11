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
import vn.springboot.dto.request.contact.ContactRequestCreateRequest;
import vn.springboot.dto.request.contact.ContactRequestSearchRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.contact.ContactRequestResponse;
import vn.springboot.entity.contact.ContactRequestEntity;
import vn.springboot.entity.enums.ContactStatus;
import vn.springboot.mapper.ContactRequestMapper;
import vn.springboot.repository.ContactRequestRepository;
import vn.springboot.repository.UserRepository;
import vn.springboot.service.impl.ContactRequestServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContactRequestServiceImplTest {

    @Mock
    private ContactRequestRepository contactRequestRepository;

    @Mock
    private ContactRequestMapper contactRequestMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ContactRequestServiceImpl service;

    private ContactRequestEntity entity(String name) {
        return ContactRequestEntity.builder().name(name).content("hello").status(ContactStatus.NEW).build();
    }

    private ContactRequestResponse response(Long id, String name) {
        return ContactRequestResponse.builder().id(id).name(name).content("hello").status(ContactStatus.NEW).build();
    }

    @Test
    void search_returnsPageResponse() {
        ContactRequestEntity e = entity("Alice");
        PageImpl<ContactRequestEntity> page =
                new PageImpl<>(List.of(e), PageRequest.of(0, 10), 1);
        when(contactRequestRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);
        when(contactRequestMapper.toResponse(e)).thenReturn(response(1L, "Alice"));

        PageResponse<ContactRequestResponse> result =
                service.search(ContactRequestSearchRequest.builder().build());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Alice");
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getPageNumber()).isEqualTo(1);
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isTrue();
    }

    @Test
    void getById_notFound_throws() {
        when(contactRequestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.CONTACT_REQUEST_NOT_FOUND);
    }

    @Test
    void create_savesRequestWithNewStatus() {
        ContactRequestCreateRequest request = ContactRequestCreateRequest.builder()
                .name("Alice")
                .email("alice@example.com")
                .content("I have a question")
                .build();
        when(contactRequestRepository.save(any(ContactRequestEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(contactRequestMapper.toResponse(any(ContactRequestEntity.class)))
                .thenAnswer(inv -> {
                    ContactRequestEntity e = inv.getArgument(0);
                    return response(1L, e.getName());
                });

        ContactRequestResponse result = service.create(request);

        assertThat(result.getName()).isEqualTo("Alice");
        verify(contactRequestRepository).save(any(ContactRequestEntity.class));
    }

    @Test
    void update_notFound_throws() {
        when(contactRequestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L,
                vn.springboot.dto.request.contact.ContactRequestUpdateRequest.builder()
                        .status(ContactStatus.HANDLED).build()))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.CONTACT_REQUEST_NOT_FOUND);
    }
}
