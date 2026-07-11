package vn.springboot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.springboot.common.exception.AppException;
import vn.springboot.common.exception.ErrorCode;
import vn.springboot.dto.request.newsletter.NewsletterSubscribeRequest;
import vn.springboot.dto.request.newsletter.NewsletterSubscriberSearchRequest;
import vn.springboot.dto.request.newsletter.NewsletterSubscriberUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.newsletter.NewsletterSubscriberResponse;
import vn.springboot.entity.newsletter.NewsletterSubscriberEntity;
import vn.springboot.mapper.NewsletterSubscriberMapper;
import vn.springboot.repository.NewsletterSubscriberRepository;
import vn.springboot.repository.specification.NewsletterSubscriberSpecification;
import vn.springboot.service.NewsletterSubscriberService;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NewsletterSubscriberServiceImpl implements NewsletterSubscriberService {

    /**
     * Whitelisted sortable columns — guards against PropertyReferenceException
     * (500) from arbitrary input.
     */
    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "email", "createdAt");
    private static final String DEFAULT_SORT_FIELD = "id";
    private static final int MAX_PAGE_SIZE = 100;

    private final NewsletterSubscriberRepository subscriberRepository;
    private final NewsletterSubscriberMapper subscriberMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NewsletterSubscriberResponse> search(NewsletterSubscriberSearchRequest request) {
        // Requests are 1-based (page=1 is first); Spring Data is 0-based.
        Pageable pageable = PageRequest.of(
                Math.max(0, request.getPage() - 1),
                Math.clamp(request.getSize(), 1, MAX_PAGE_SIZE),
                resolveSort(request));
        Specification<NewsletterSubscriberEntity> specification =
                NewsletterSubscriberSpecification.build(request);

        Page<NewsletterSubscriberEntity> page = subscriberRepository.findAll(specification, pageable);

        List<NewsletterSubscriberResponse> content = page.getContent().stream()
                .map(subscriberMapper::toResponse)
                .toList();

        return PageResponse.<NewsletterSubscriberResponse>builder()
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
    public NewsletterSubscriberResponse getById(Long id) {
        return subscriberRepository.findById(id)
                .map(subscriberMapper::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.NEWSLETTER_SUBSCRIBER_NOT_FOUND));
    }

    @Override
    @Transactional
    public NewsletterSubscriberResponse subscribe(NewsletterSubscribeRequest request) {
        if (subscriberRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.NEWSLETTER_EMAIL_EXISTED);
        }

        NewsletterSubscriberEntity entity = NewsletterSubscriberEntity.builder()
                .email(request.getEmail())
                .isActive(true)
                .build();

        return subscriberMapper.toResponse(subscriberRepository.save(entity));
    }

    @Override
    @Transactional
    public NewsletterSubscriberResponse update(Long id, NewsletterSubscriberUpdateRequest request) {
        NewsletterSubscriberEntity entity = subscriberRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NEWSLETTER_SUBSCRIBER_NOT_FOUND));

        if (request.getIsActive() != null) {
            entity.setActive(request.getIsActive());
        }

        return subscriberMapper.toResponse(subscriberRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        NewsletterSubscriberEntity entity = subscriberRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NEWSLETTER_SUBSCRIBER_NOT_FOUND));

        subscriberRepository.delete(entity);
    }

    private Sort resolveSort(NewsletterSubscriberSearchRequest request) {
        String field = SORTABLE_FIELDS.contains(request.getSortBy()) ? request.getSortBy() : DEFAULT_SORT_FIELD;
        Sort.Direction direction = "DESC".equalsIgnoreCase(request.getSortDirection())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return Sort.by(direction, field);
    }
}
