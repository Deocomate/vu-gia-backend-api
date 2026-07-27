package vn.springboot.common.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import vn.springboot.dto.response.PageResponse;

import java.util.List;
import java.util.Set;

/**
 * Shared pagination/sort-resolution logic, extracted from the near-identical
 * blocks that used to be copy-pasted across every {@code *ServiceImpl} that
 * exposes a paginated search endpoint.
 *
 * <p>Behavior is intentionally unchanged from the pre-extraction code: page is
 * 1-based at the API boundary and converted to 0-based for Spring Data, size is
 * clamped to {@code [1, 100]}, and the sort field/direction fall back to
 * caller-supplied defaults (never hardcoded), since defaults differ per domain
 * (e.g. {@code OrderSearchRequest} defaults {@code sortDirection} to
 * {@code DESC}, {@code ShippingMethodSearchRequest} defaults {@code sortBy} to
 * {@code sortOrder}).
 */
public final class PaginationUtils {

    private static final int MAX_PAGE_SIZE = 100;

    private PaginationUtils() {
    }

    /**
     * Builds a Spring Data {@link Pageable} from 1-based API pagination
     * parameters, whitelisting the requested sort field and direction.
     *
     * @param page              1-based page number as received from the client
     * @param size              requested page size, clamped to {@code [1, 100]}
     * @param sortBy            requested sort field; must be present in {@code sortableFields}
     *                          or the call falls back to {@code defaultSortField}
     * @param sortDirection     requested sort direction ("ASC"/"DESC", case-insensitive);
     *                          anything else falls back to {@code defaultSortDirection}
     * @param sortableFields    whitelist of sortable columns for this call site
     * @param defaultSortField  fallback sort field when {@code sortBy} is not whitelisted
     * @param defaultSortDirection fallback direction when {@code sortDirection} is neither
     *                             "ASC" nor "DESC" (case-insensitive)
     */
    public static Pageable buildPageable(int page,
                                          int size,
                                          String sortBy,
                                          String sortDirection,
                                          Set<String> sortableFields,
                                          String defaultSortField,
                                          String defaultSortDirection) {
        int pageIndex = Math.max(0, page - 1);
        int clampedSize = Math.clamp(size, 1, MAX_PAGE_SIZE);
        Sort sort = resolveSort(sortBy, sortDirection, sortableFields, defaultSortField, defaultSortDirection);
        return PageRequest.of(pageIndex, clampedSize, sort);
    }

    private static Sort resolveSort(String sortBy,
                                     String sortDirection,
                                     Set<String> sortableFields,
                                     String defaultSortField,
                                     String defaultSortDirection) {
        String field = sortableFields.contains(sortBy) ? sortBy : defaultSortField;
        Sort.Direction direction = resolveDirection(sortDirection, defaultSortDirection);
        return Sort.by(direction, field);
    }

    private static Sort.Direction resolveDirection(String sortDirection, String defaultSortDirection) {
        if ("DESC".equalsIgnoreCase(sortDirection)) {
            return Sort.Direction.DESC;
        }
        if ("ASC".equalsIgnoreCase(sortDirection)) {
            return Sort.Direction.ASC;
        }
        return "DESC".equalsIgnoreCase(defaultSortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
    }

    /**
     * Assembles a {@link PageResponse} from a Spring Data {@link Page} and its
     * already-mapped content, using the same 1-based page-numbering convention
     * as {@link #buildPageable}.
     */
    public static <T> PageResponse<T> toPageResponse(Page<?> page, List<T> content) {
        return PageResponse.<T>builder()
                .content(content)
                .pageNumber(page.getNumber() + 1)
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
