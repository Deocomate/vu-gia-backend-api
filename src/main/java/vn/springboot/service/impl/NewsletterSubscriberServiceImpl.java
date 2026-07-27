package vn.springboot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.springboot.common.exception.AppException;
import vn.springboot.common.exception.ErrorCode;
import vn.springboot.common.util.PaginationUtils;
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
    private static final String DEFAULT_SORT_DIRECTION = "ASC";

    private final NewsletterSubscriberRepository subscriberRepository;
    private final NewsletterSubscriberMapper subscriberMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NewsletterSubscriberResponse> search(NewsletterSubscriberSearchRequest request) {
        Pageable pageable = PaginationUtils.buildPageable(
                request.getPage(), request.getSize(), request.getSortBy(), request.getSortDirection(),
                SORTABLE_FIELDS, DEFAULT_SORT_FIELD, DEFAULT_SORT_DIRECTION);
        Specification<NewsletterSubscriberEntity> specification =
                NewsletterSubscriberSpecification.build(request);

        Page<NewsletterSubscriberEntity> page = subscriberRepository.findAll(specification, pageable);

        List<NewsletterSubscriberResponse> content = page.getContent().stream()
                .map(subscriberMapper::toResponse)
                .toList();

        return PaginationUtils.toPageResponse(page, content);
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
}
