package vn.springboot.common.util;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import vn.springboot.dto.response.PageResponse;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PaginationUtilsTest {

    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "name", "createdAt");

    @Test
    void buildPageable_clampsPageZeroToFirstPage() {
        Pageable pageable = PaginationUtils.buildPageable(0, 20, "id", "ASC", SORTABLE_FIELDS, "id", "ASC");
        assertThat(pageable.getPageNumber()).isZero();
    }

    @Test
    void buildPageable_clampsNegativePageToFirstPage() {
        Pageable pageable = PaginationUtils.buildPageable(-5, 20, "id", "ASC", SORTABLE_FIELDS, "id", "ASC");
        assertThat(pageable.getPageNumber()).isZero();
    }

    @Test
    void buildPageable_convertsOneBasedPageToZeroBasedIndex() {
        Pageable pageable = PaginationUtils.buildPageable(3, 20, "id", "ASC", SORTABLE_FIELDS, "id", "ASC");
        assertThat(pageable.getPageNumber()).isEqualTo(2);
    }

    @Test
    void buildPageable_clampsSizeAboveMaxTo100() {
        Pageable pageable = PaginationUtils.buildPageable(1, 500, "id", "ASC", SORTABLE_FIELDS, "id", "ASC");
        assertThat(pageable.getPageSize()).isEqualTo(100);
    }

    @Test
    void buildPageable_clampsSizeBelowOneToOne() {
        Pageable pageable = PaginationUtils.buildPageable(1, 0, "id", "ASC", SORTABLE_FIELDS, "id", "ASC");
        assertThat(pageable.getPageSize()).isEqualTo(1);

        Pageable negative = PaginationUtils.buildPageable(1, -10, "id", "ASC", SORTABLE_FIELDS, "id", "ASC");
        assertThat(negative.getPageSize()).isEqualTo(1);
    }

    @Test
    void buildPageable_fallsBackToDefaultSortFieldWhenNotWhitelisted() {
        Pageable pageable = PaginationUtils.buildPageable(1, 20, "notAllowed", "ASC", SORTABLE_FIELDS, "createdAt", "ASC");
        Sort.Order order = pageable.getSort().getOrderFor("createdAt");
        assertThat(order).isNotNull();
    }

    @Test
    void buildPageable_honorsSortFieldWhenWhitelisted() {
        Pageable pageable = PaginationUtils.buildPageable(1, 20, "name", "ASC", SORTABLE_FIELDS, "id", "ASC");
        Sort.Order order = pageable.getSort().getOrderFor("name");
        assertThat(order).isNotNull();
    }

    @Test
    void buildPageable_honorsExplicitDescDirection() {
        Pageable pageable = PaginationUtils.buildPageable(1, 20, "id", "DESC", SORTABLE_FIELDS, "id", "ASC");
        assertThat(pageable.getSort().getOrderFor("id").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void buildPageable_fallsBackToNonAscDefaultDirection() {
        // Mirrors OrderSearchRequest/OrderAdminSearchRequest, whose default sortDirection is DESC.
        Pageable pageable = PaginationUtils.buildPageable(1, 20, "invalidDirection", "id", SORTABLE_FIELDS, "id", "DESC");
        assertThat(pageable.getSort().getOrderFor("id").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void buildPageable_fallsBackToAscDefaultDirectionWhenInvalid() {
        Pageable pageable = PaginationUtils.buildPageable(1, 20, "id", "sideways", SORTABLE_FIELDS, "id", "ASC");
        assertThat(pageable.getSort().getOrderFor("id").getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void toPageResponse_mapsOneBasedPageNumberMatchingPreviousManualBuilder() {
        Pageable pageable = PageRequest.of(2, 10); // zero-based index 2 == 1-based page 3
        // Content size must equal the page size here: Spring's PageImpl recomputes
        // totalElements from (offset + content.size()) whenever content is smaller
        // than the page size, which would otherwise mask the assertion below.
        Page<String> page = new PageImpl<>(List.of("a", "b", "c", "d", "e", "f", "g", "h", "i", "j"), pageable, 35);

        PageResponse<String> response = PaginationUtils.toPageResponse(page, page.getContent());

        assertThat(response.getPageNumber()).isEqualTo(3);
        assertThat(response.getPageSize()).isEqualTo(10);
        assertThat(response.getTotalElements()).isEqualTo(35);
        assertThat(response.getTotalPages()).isEqualTo(page.getTotalPages());
        assertThat(response.isFirst()).isEqualTo(page.isFirst());
        assertThat(response.isLast()).isEqualTo(page.isLast());
        assertThat(response.getContent()).hasSize(10).startsWith("a", "b");
    }
}
