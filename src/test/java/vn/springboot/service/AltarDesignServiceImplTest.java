package vn.springboot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import vn.springboot.common.exception.AppException;
import vn.springboot.common.exception.ErrorCode;
import vn.springboot.dto.request.altar.AltarDesignRenameRequest;
import vn.springboot.dto.request.altar.AltarDesignRequest;
import vn.springboot.dto.request.altar.AltarDesignSearchRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.altar.AltarDesignResponse;
import vn.springboot.entity.altar.AltarDesignEntity;
import vn.springboot.entity.altar.AltarModelEntity;
import vn.springboot.entity.altar.AltarModelSizeEntity;
import vn.springboot.entity.enums.ProductStatus;
import vn.springboot.entity.enums.Role;
import vn.springboot.entity.product.ProductEntity;
import vn.springboot.entity.user.UserEntity;
import vn.springboot.mapper.AltarDesignMapper;
import vn.springboot.repository.AltarDesignRepository;
import vn.springboot.repository.AltarModelSizeRepository;
import vn.springboot.repository.AltarStyleRepository;
import vn.springboot.repository.ProductRepository;
import vn.springboot.security.CustomUserDetails;
import vn.springboot.service.impl.AltarDesignServiceImpl;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Security-critical: this is the second per-user-ownership-checked resource in the codebase
 * (after {@code OrderServiceImplTest}). The ownership-isolation tests below are the highest-stakes
 * assertions in this test class — they must fail loudly (404, never 403) if that contract regresses.
 */
@ExtendWith(MockitoExtension.class)
class AltarDesignServiceImplTest {

    @Mock private AltarDesignRepository altarDesignRepository;
    @Mock private AltarModelSizeRepository altarModelSizeRepository;
    @Mock private AltarStyleRepository altarStyleRepository;
    @Mock private ProductRepository productRepository;
    @Mock private AltarDesignMapper altarDesignMapper;

    // Real instance, not a Mockito mock: the service does real JSON parsing/serialization
    // (readTree/writeValueAsString) on items/accessories, which a mock can't fake convincingly.
    private final ObjectMapper objectMapper = new ObjectMapper();

    private AltarDesignServiceImpl service;

    private UserEntity owner;
    private UserEntity stranger;

    @BeforeEach
    void setUp() {
        service = new AltarDesignServiceImpl(
                altarDesignRepository, altarModelSizeRepository, altarStyleRepository,
                productRepository, altarDesignMapper, objectMapper);
        owner = user(1L);
        stranger = user(2L);
        authenticate(owner);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private UserEntity user(Long id) {
        UserEntity u = new UserEntity();
        u.setId(id);
        u.setRole(Role.CUSTOMER);
        return u;
    }

    private void authenticate(UserEntity u) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new CustomUserDetails(u), null, List.of()));
    }

    private AltarModelSizeEntity altarModelSize(Long id) {
        AltarModelEntity model = AltarModelEntity.builder().name("Ban tho gia tien").build();
        model.setId(100L);
        AltarModelSizeEntity size = AltarModelSizeEntity.builder().altarModel(model).label("127 x 61 cm").build();
        size.setId(id);
        return size;
    }

    private ProductEntity product(Long id, long price, ProductStatus status) {
        ProductEntity p = new ProductEntity();
        p.setId(id);
        p.setPrice(price);
        p.setStatus(status);
        return p;
    }

    private AltarDesignEntity design(Long id, UserEntity owningUser, String items, String accessories, long totalPrice) {
        AltarDesignEntity e = AltarDesignEntity.builder()
                .user(owningUser)
                .name("My design")
                .thumb("/files/thumb.png")
                .altarModelSize(altarModelSize(5L))
                .items(items)
                .accessories(accessories)
                .totalPrice(totalPrice)
                .build();
        e.setId(id);
        return e;
    }

    private AltarDesignRequest.AltarDesignRequestBuilder validRequestBuilder() {
        return AltarDesignRequest.builder()
                .name("My design")
                .thumb("/files/thumb.png")
                .altarModelSizeId(5L)
                .items("[{\"productId\":10,\"quantity\":1,\"x\":0.5,\"y\":0.5}]")
                .accessories("[]")
                .totalPrice(100_000L);
    }

    private void stubMapperPassthrough() {
        when(altarDesignMapper.toResponse(any(AltarDesignEntity.class))).thenAnswer(invocation -> {
            AltarDesignEntity e = invocation.getArgument(0);
            return AltarDesignResponse.builder()
                    .id(e.getId())
                    .name(e.getName())
                    .thumb(e.getThumb())
                    .altarModelSizeId(e.getAltarModelSize() != null ? e.getAltarModelSize().getId() : null)
                    .totalPrice(e.getTotalPrice())
                    .createdAt(e.getCreatedAt())
                    .updatedAt(e.getUpdatedAt())
                    .build();
        });
    }

    // ----- Ownership isolation (highest-stakes: 404, never 403) -----

    @Test
    void getById_foreignDesign_returns404_notForbidden() {
        AltarDesignEntity foreign = design(9L, stranger, "[]", "[]", 0L);
        when(altarDesignRepository.findById(9L)).thenReturn(Optional.of(foreign));

        assertThatThrownBy(() -> service.getById(9L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ALTAR_DESIGN_NOT_FOUND);
        assertThat(ErrorCode.ALTAR_DESIGN_NOT_FOUND.getHttpStatus())
                .as("must hide existence with 404, never leak it with 403")
                .isEqualTo(HttpStatus.NOT_FOUND)
                .isNotEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void rename_foreignDesign_returns404_notForbidden() {
        AltarDesignEntity foreign = design(9L, stranger, "[]", "[]", 0L);
        when(altarDesignRepository.findById(9L)).thenReturn(Optional.of(foreign));

        assertThatThrownBy(() -> service.rename(9L, AltarDesignRenameRequest.builder().name("x").build()))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ALTAR_DESIGN_NOT_FOUND);
        assertThat(ErrorCode.ALTAR_DESIGN_NOT_FOUND.getHttpStatus()).isNotEqualTo(HttpStatus.FORBIDDEN);
        verify(altarDesignRepository, never()).save(any());
    }

    @Test
    void delete_foreignDesign_returns404_notForbidden() {
        AltarDesignEntity foreign = design(9L, stranger, "[]", "[]", 0L);
        when(altarDesignRepository.findById(9L)).thenReturn(Optional.of(foreign));

        assertThatThrownBy(() -> service.delete(9L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ALTAR_DESIGN_NOT_FOUND);
        assertThat(ErrorCode.ALTAR_DESIGN_NOT_FOUND.getHttpStatus()).isNotEqualTo(HttpStatus.FORBIDDEN);
        verify(altarDesignRepository, never()).delete(any());
    }

    @Test
    void getById_ownDesign_succeeds() {
        AltarDesignEntity mine = design(9L, owner, "[]", "[]", 0L);
        when(altarDesignRepository.findById(9L)).thenReturn(Optional.of(mine));
        stubMapperPassthrough();

        AltarDesignResponse response = service.getById(9L);

        assertThat(response.getId()).isEqualTo(9L);
    }

    @Test
    void notFound_id_returns404() {
        when(altarDesignRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ALTAR_DESIGN_NOT_FOUND);
    }

    @Test
    void unauthenticated_throwsUnauthenticated() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> service.list(AltarDesignSearchRequest.builder().build()))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.UNAUTHENTICATED);
    }

    // ----- 20-design cap (decision D5) -----

    @Test
    void create_atCap_throwsLimitReached() {
        when(altarDesignRepository.countByUser_Id(1L)).thenReturn(20L);

        assertThatThrownBy(() -> service.create(validRequestBuilder().build()))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ALTAR_DESIGN_LIMIT_REACHED);
        verify(altarDesignRepository, never()).save(any());
    }

    @Test
    void create_underCap_succeeds() {
        when(altarDesignRepository.countByUser_Id(1L)).thenReturn(19L);
        when(productRepository.findAllById(Set.of(10L))).thenReturn(List.of(product(10L, 50_000L, ProductStatus.PUBLISHED)));
        when(altarModelSizeRepository.findById(5L)).thenReturn(Optional.of(altarModelSize(5L)));
        when(altarDesignRepository.save(any(AltarDesignEntity.class))).thenAnswer(invocation -> {
            AltarDesignEntity e = invocation.getArgument(0);
            e.setId(1L);
            return e;
        });
        stubMapperPassthrough();

        AltarDesignResponse response = service.create(validRequestBuilder().build());

        assertThat(response.getId()).isEqualTo(1L);
        verify(altarDesignRepository).save(any());
    }

    // ----- Payload validation -----

    @Test
    void create_coordinateOutOfRange_rejected() {
        AltarDesignRequest request = validRequestBuilder()
                .items("[{\"productId\":10,\"x\":1.5,\"y\":0.5}]")
                .build();

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.VALIDATION_ERROR);
        verify(altarDesignRepository, never()).save(any());
    }

    @Test
    void create_unknownProductId_rejected() {
        when(productRepository.findAllById(Set.of(10L))).thenReturn(List.of());

        assertThatThrownBy(() -> service.create(validRequestBuilder().build()))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.VALIDATION_ERROR);
        verify(altarDesignRepository, never()).save(any());
    }

    @Test
    void create_oversizedItemsArray_rejected() {
        StringBuilder items = new StringBuilder("[");
        for (int i = 0; i < 201; i++) {
            if (i > 0) items.append(',');
            items.append("{\"productId\":").append(i + 1).append('}');
        }
        items.append(']');

        AltarDesignRequest request = validRequestBuilder().items(items.toString()).build();

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.VALIDATION_ERROR);
        verify(altarDesignRepository, never()).save(any());
    }

    // ----- Price snapshot / recompute -----

    @Test
    void getById_priceChangedSinceSave_currentTotalPriceDiffersFromSnapshot() {
        String items = "[{\"productId\":10,\"quantity\":2}]";
        AltarDesignEntity entity = design(9L, owner, items, "[]", 100_000L);
        when(altarDesignRepository.findById(9L)).thenReturn(Optional.of(entity));
        // Live price is now 60,000 (was 50,000 at save time) => current total 120,000 != stored 100,000.
        when(productRepository.findAllById(Set.of(10L)))
                .thenReturn(List.of(product(10L, 60_000L, ProductStatus.PUBLISHED)));
        stubMapperPassthrough();

        AltarDesignResponse response = service.getById(9L);

        assertThat(response.getTotalPrice()).isEqualTo(100_000L);
        assertThat(response.getCurrentTotalPrice()).isEqualTo(120_000L);
        assertThat(response.getCurrentTotalPrice()).isNotEqualTo(response.getTotalPrice());
        assertThat(response.getDroppedItemCount()).isZero();
    }

    // ----- Deleted / unpublished products dropped on open -----

    @Test
    void getById_productNoLongerPublished_isDroppedAndCounted() {
        String items = "[{\"productId\":10,\"quantity\":1},{\"productId\":11,\"quantity\":1}]";
        AltarDesignEntity entity = design(9L, owner, items, "[]", 100_000L);
        when(altarDesignRepository.findById(9L)).thenReturn(Optional.of(entity));
        // Product 10 still published; product 11 was deleted (absent from the live lookup entirely).
        when(productRepository.findAllById(Set.of(10L, 11L)))
                .thenReturn(List.of(product(10L, 50_000L, ProductStatus.PUBLISHED)));
        stubMapperPassthrough();

        AltarDesignResponse response = service.getById(9L);

        assertThat(response.getDroppedItemCount()).isEqualTo(1);
        assertThat(response.getItems()).contains("\"productId\":10").doesNotContain("\"productId\":11");
        assertThat(response.getCurrentTotalPrice()).isEqualTo(50_000L);
    }

    @Test
    void getById_unpublishedProduct_isDroppedAndCounted() {
        String items = "[{\"productId\":10,\"quantity\":1}]";
        AltarDesignEntity entity = design(9L, owner, items, "[]", 50_000L);
        when(altarDesignRepository.findById(9L)).thenReturn(Optional.of(entity));
        when(productRepository.findAllById(Set.of(10L)))
                .thenReturn(List.of(product(10L, 50_000L, ProductStatus.DRAFT)));
        stubMapperPassthrough();

        AltarDesignResponse response = service.getById(9L);

        assertThat(response.getDroppedItemCount()).isEqualTo(1);
        assertThat(response.getCurrentTotalPrice()).isZero();
    }

    // ----- List is scoped to the caller -----

    @Test
    void list_scopedToCurrentUser() {
        AltarDesignEntity mine = design(1L, owner, "[]", "[]", 0L);
        PageImpl<AltarDesignEntity> page = new PageImpl<>(List.of(mine), PageRequest.of(0, 20), 1);
        when(altarDesignRepository.findByUser_Id(anyLong(), any())).thenReturn(page);

        PageResponse<?> result = service.list(AltarDesignSearchRequest.builder().build());

        assertThat(result.getContent()).hasSize(1);
        verify(altarDesignRepository).findByUser_Id(eq(1L), any());
    }
}
