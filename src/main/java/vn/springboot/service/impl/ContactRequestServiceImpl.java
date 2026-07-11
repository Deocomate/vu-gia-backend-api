package vn.springboot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.springboot.common.exception.AppException;
import vn.springboot.common.exception.ErrorCode;
import vn.springboot.dto.request.contact.ContactRequestCreateRequest;
import vn.springboot.dto.request.contact.ContactRequestSearchRequest;
import vn.springboot.dto.request.contact.ContactRequestUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.contact.ContactRequestResponse;
import vn.springboot.entity.contact.ContactRequestEntity;
import vn.springboot.entity.enums.ContactStatus;
import vn.springboot.mapper.ContactRequestMapper;
import vn.springboot.repository.ContactRequestRepository;
import vn.springboot.repository.UserRepository;
import vn.springboot.repository.specification.ContactRequestSpecification;
import vn.springboot.service.ContactRequestService;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ContactRequestServiceImpl implements ContactRequestService {

    /**
     * Whitelisted sortable columns — guards against PropertyReferenceException
     * (500) from arbitrary input.
     */
    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "name", "status", "createdAt");
    private static final String DEFAULT_SORT_FIELD = "id";
    private static final int MAX_PAGE_SIZE = 100;

    private final ContactRequestRepository contactRequestRepository;
    private final ContactRequestMapper contactRequestMapper;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ContactRequestResponse> search(ContactRequestSearchRequest request) {
        // Requests are 1-based (page=1 is first); Spring Data is 0-based.
        Pageable pageable = PageRequest.of(
                Math.max(0, request.getPage() - 1),
                Math.clamp(request.getSize(), 1, MAX_PAGE_SIZE),
                resolveSort(request));
        Specification<ContactRequestEntity> specification =
                ContactRequestSpecification.build(request);

        Page<ContactRequestEntity> page = contactRequestRepository.findAll(specification, pageable);

        List<ContactRequestResponse> content = page.getContent().stream()
                .map(contactRequestMapper::toResponse)
                .toList();

        return PageResponse.<ContactRequestResponse>builder()
                .content(content)
                .pageNumber(page.getNumber() + 1)
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ContactRequestResponse getById(Long id) {
        return contactRequestRepository.findById(id)
                .map(contactRequestMapper::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.CONTACT_REQUEST_NOT_FOUND));
    }

    @Override
    @Transactional
    public ContactRequestResponse create(ContactRequestCreateRequest request) {
        ContactRequestEntity entity = ContactRequestEntity.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .content(request.getContent())
                .status(ContactStatus.NEW)
                .build();

        return contactRequestMapper.toResponse(contactRequestRepository.save(entity));
    }

    @Override
    @Transactional
    public ContactRequestResponse update(Long id, ContactRequestUpdateRequest request) {
        ContactRequestEntity entity = contactRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CONTACT_REQUEST_NOT_FOUND));

        entity.setStatus(request.getStatus());
        // Stamp the staff member acting on the request once it leaves the NEW state.
        if (request.getStatus() != ContactStatus.NEW) {
            currentUsername().flatMap(userRepository::findByUsername)
                    .ifPresent(entity::setHandledBy);
        }

        return contactRequestMapper.toResponse(contactRequestRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ContactRequestEntity entity = contactRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CONTACT_REQUEST_NOT_FOUND));

        contactRequestRepository.delete(entity);
    }

    private java.util.Optional<String> currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.ofNullable(authentication.getName());
    }

    private Sort resolveSort(ContactRequestSearchRequest request) {
        String field = SORTABLE_FIELDS.contains(request.getSortBy()) ? request.getSortBy() : DEFAULT_SORT_FIELD;
        Sort.Direction direction = "DESC".equalsIgnoreCase(request.getSortDirection())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return Sort.by(direction, field);
    }
}
