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
import vn.springboot.dto.request.faq.FaqCreateRequest;
import vn.springboot.dto.request.faq.FaqSearchRequest;
import vn.springboot.dto.request.faq.FaqUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.faq.FaqResponse;
import vn.springboot.entity.faq.FaqEntity;
import vn.springboot.mapper.FaqMapper;
import vn.springboot.repository.FaqRepository;
import vn.springboot.repository.specification.FaqSpecification;
import vn.springboot.service.FaqService;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FaqServiceImpl implements FaqService {

    /**
     * Whitelisted sortable columns — guards against PropertyReferenceException
     * (500) from arbitrary input.
     */
    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "sortOrder", "createdAt");
    private static final String DEFAULT_SORT_FIELD = "id";
    private static final int MAX_PAGE_SIZE = 100;

    private final FaqRepository faqRepository;
    private final FaqMapper faqMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FaqResponse> search(FaqSearchRequest request) {
        // Requests are 1-based (page=1 is first); Spring Data is 0-based.
        Pageable pageable = PageRequest.of(
                Math.max(0, request.getPage() - 1),
                Math.clamp(request.getSize(), 1, MAX_PAGE_SIZE),
                resolveSort(request));
        Specification<FaqEntity> specification = FaqSpecification.build(request);

        Page<FaqEntity> page = faqRepository.findAll(specification, pageable);

        List<FaqResponse> content = page.getContent().stream()
                .map(faqMapper::toResponse)
                .toList();

        return PageResponse.<FaqResponse>builder()
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
    public FaqResponse getById(Long id) {
        return faqRepository.findById(id)
                .map(faqMapper::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.FAQ_NOT_FOUND));
    }

    @Override
    @Transactional
    public FaqResponse create(FaqCreateRequest request) {
        FaqEntity entity = FaqEntity.builder()
                .question(request.getQuestion())
                .answer(request.getAnswer())
                .category(request.getCategory())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        return faqMapper.toResponse(faqRepository.save(entity));
    }

    @Override
    @Transactional
    public FaqResponse update(Long id, FaqUpdateRequest request) {
        FaqEntity entity = faqRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.FAQ_NOT_FOUND));

        if (request.getQuestion() != null) {
            entity.setQuestion(request.getQuestion());
        }
        if (request.getAnswer() != null) {
            entity.setAnswer(request.getAnswer());
        }
        if (request.getCategory() != null) {
            entity.setCategory(request.getCategory());
        }
        if (request.getSortOrder() != null) {
            entity.setSortOrder(request.getSortOrder());
        }
        if (request.getIsActive() != null) {
            entity.setActive(request.getIsActive());
        }

        return faqMapper.toResponse(faqRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        FaqEntity entity = faqRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.FAQ_NOT_FOUND));

        faqRepository.delete(entity);
    }

    private Sort resolveSort(FaqSearchRequest request) {
        String field = SORTABLE_FIELDS.contains(request.getSortBy()) ? request.getSortBy() : DEFAULT_SORT_FIELD;
        Sort.Direction direction = "DESC".equalsIgnoreCase(request.getSortDirection())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return Sort.by(direction, field);
    }
}
