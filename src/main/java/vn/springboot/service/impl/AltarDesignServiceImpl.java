package vn.springboot.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.springboot.common.exception.AppException;
import vn.springboot.common.exception.ErrorCode;
import vn.springboot.common.util.PaginationUtils;
import vn.springboot.dto.request.altar.AltarDesignRenameRequest;
import vn.springboot.dto.request.altar.AltarDesignRequest;
import vn.springboot.dto.request.altar.AltarDesignSearchRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.altar.AltarDesignResponse;
import vn.springboot.dto.response.altar.AltarDesignSummaryResponse;
import vn.springboot.entity.altar.AltarDesignEntity;
import vn.springboot.entity.altar.AltarModelSizeEntity;
import vn.springboot.entity.altar.AltarStyleEntity;
import vn.springboot.entity.enums.ProductStatus;
import vn.springboot.entity.product.ProductEntity;
import vn.springboot.entity.user.UserEntity;
import vn.springboot.mapper.AltarDesignMapper;
import vn.springboot.repository.AltarDesignRepository;
import vn.springboot.repository.AltarModelSizeRepository;
import vn.springboot.repository.AltarStyleRepository;
import vn.springboot.repository.ProductRepository;
import vn.springboot.security.CustomUserDetails;
import vn.springboot.service.AltarDesignService;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * This is this codebase's second per-user-ownership-checked resource (after
 * {@link vn.springboot.service.impl.OrderServiceImpl}) and the first one with no staff-override —
 * a design is strictly the owner's. Every read/write resolves the caller via {@link #currentUser()}
 * and, for single-row operations, loads by id alone via the repository's inherited
 * {@code findById}, then explicitly compares {@code entity.getUser().getId()} against the caller —
 * throwing {@link ErrorCode#ALTAR_DESIGN_NOT_FOUND} (404) on a mismatch, never a 403, so a foreign
 * id's existence is never leaked. Do not "optimize" this into a single scoped repository query —
 * the explicit check is the one place this contract is tested.
 */
@Service
@RequiredArgsConstructor
public class AltarDesignServiceImpl implements AltarDesignService {

    /** Decision D5: a customer may store at most this many designs. */
    private static final int MAX_DESIGNS_PER_USER = 20;

    /** Sane upper bound on a single canvas arrangement — generous for any real save, cheap to
     * validate, and prevents an unbounded array from ballooning the JSON column. */
    private static final int MAX_ITEMS_COUNT = 200;

    /** Per-field (items/accessories independently) serialized-size cap in bytes. */
    private static final int MAX_JSON_BYTES = 64 * 1024;

    /** Generous static surface-relative bound for an item's x/y — see
     * {@code AltarPlacementRequest}'s docblock for why this isn't per-altar-size-exact. */
    private static final double UNIT_RANGE_MIN = -2.0;
    private static final double UNIT_RANGE_MAX = 3.0;

    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "name", "createdAt", "totalPrice");
    private static final String DEFAULT_SORT_FIELD = "createdAt";
    private static final String DEFAULT_SORT_DIRECTION = "DESC";

    private final AltarDesignRepository altarDesignRepository;
    private final AltarModelSizeRepository altarModelSizeRepository;
    private final AltarStyleRepository altarStyleRepository;
    private final ProductRepository productRepository;
    private final AltarDesignMapper altarDesignMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AltarDesignSummaryResponse> list(AltarDesignSearchRequest request) {
        UserEntity user = currentUser();
        Pageable pageable = PaginationUtils.buildPageable(
                request.getPage(), request.getSize(), request.getSortBy(), request.getSortDirection(),
                SORTABLE_FIELDS, DEFAULT_SORT_FIELD, DEFAULT_SORT_DIRECTION);

        Page<AltarDesignEntity> page = altarDesignRepository.findByUser_Id(user.getId(), pageable);
        List<AltarDesignSummaryResponse> content = page.getContent().stream()
                .map(this::toSummaryResponse)
                .toList();

        return PaginationUtils.toPageResponse(page, content);
    }

    @Override
    @Transactional(readOnly = true)
    public AltarDesignResponse getById(Long id) {
        UserEntity user = currentUser();
        AltarDesignEntity entity = findOwnedOrThrow(id, user);
        return toDetailResponse(entity);
    }

    @Override
    @Transactional
    public AltarDesignResponse create(AltarDesignRequest request) {
        UserEntity user = currentUser();

        if (altarDesignRepository.countByUser_Id(user.getId()) >= MAX_DESIGNS_PER_USER) {
            throw new AppException(ErrorCode.ALTAR_DESIGN_LIMIT_REACHED);
        }

        validateDesignPayload(request.getItems(), request.getAccessories());

        AltarModelSizeEntity altarModelSize = findAltarModelSizeOrThrow(request.getAltarModelSizeId());
        AltarStyleEntity altarStyle = resolveAltarStyle(request.getAltarStyleId());

        AltarDesignEntity entity = AltarDesignEntity.builder()
                .user(user)
                .name(request.getName().trim())
                .thumb(request.getThumb().trim())
                .altarModelSize(altarModelSize)
                .altarStyle(altarStyle)
                .items(request.getItems())
                .accessories(request.getAccessories())
                .totalPrice(request.getTotalPrice())
                .build();

        AltarDesignEntity saved = altarDesignRepository.save(entity);
        return toDetailResponse(saved);
    }

    @Override
    @Transactional
    public AltarDesignResponse rename(Long id, AltarDesignRenameRequest request) {
        UserEntity user = currentUser();
        AltarDesignEntity entity = findOwnedOrThrow(id, user);

        entity.setName(request.getName().trim());
        AltarDesignEntity saved = altarDesignRepository.save(entity);
        return toDetailResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        UserEntity user = currentUser();
        AltarDesignEntity entity = findOwnedOrThrow(id, user);
        altarDesignRepository.delete(entity);
    }

    /**
     * Loads by id alone, then explicitly checks ownership — 404 (never 403) on a foreign id so
     * existence is never leaked. See class javadoc for why this is not a scoped repository query.
     */
    private AltarDesignEntity findOwnedOrThrow(Long id, UserEntity user) {
        AltarDesignEntity entity = altarDesignRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ALTAR_DESIGN_NOT_FOUND));
        if (!entity.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.ALTAR_DESIGN_NOT_FOUND);
        }
        return entity;
    }

    /**
     * Validates client-supplied {@code items}/{@code accessories} JSON before it is ever persisted:
     * each must be a JSON array, bounded in both entry count and serialized byte size, every entry
     * must carry a {@code productId} that resolves to an existing product, and any {@code x}/{@code y}
     * present on an entry must fall within {@link #UNIT_RANGE_MIN}/{@link #UNIT_RANGE_MAX}
     * (surface-relative coordinates — see {@code AltarPlacementRequest}'s docblock for why this is
     * a generous static bound, not per-size-exact). Rejects with {@link ErrorCode#VALIDATION_ERROR}
     * (400) on any violation — never silently coerced or dropped at write time (drop-on-read is a
     * separate, read-only concern — see {@link #recompute}).
     */
    private void validateDesignPayload(String items, String accessories) {
        Set<Long> productIds = new HashSet<>();
        productIds.addAll(validateItemsArray(items, "items"));
        productIds.addAll(validateItemsArray(accessories, "accessories"));

        if (!productIds.isEmpty()) {
            Set<Long> existingIds = productRepository.findAllById(productIds).stream()
                    .map(ProductEntity::getId)
                    .collect(Collectors.toSet());
            if (!existingIds.containsAll(productIds)) {
                throw new AppException(ErrorCode.VALIDATION_ERROR,
                        "One or more referenced products do not exist");
            }
        }
    }

    private Set<Long> validateItemsArray(String json, String fieldName) {
        if (json.getBytes(StandardCharsets.UTF_8).length > MAX_JSON_BYTES) {
            throw new AppException(ErrorCode.VALIDATION_ERROR,
                    fieldName + " payload exceeds the maximum allowed size");
        }

        JsonNode root;
        try {
            root = objectMapper.readTree(json);
        } catch (Exception e) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, fieldName + " is not valid JSON");
        }
        if (!root.isArray()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, fieldName + " must be a JSON array");
        }
        if (root.size() > MAX_ITEMS_COUNT) {
            throw new AppException(ErrorCode.VALIDATION_ERROR,
                    fieldName + " exceeds the maximum of " + MAX_ITEMS_COUNT + " entries");
        }

        Set<Long> productIds = new HashSet<>();
        for (JsonNode node : root) {
            if (!node.isObject() || !node.hasNonNull("productId")) {
                throw new AppException(ErrorCode.VALIDATION_ERROR,
                        fieldName + " entries must each carry a productId");
            }
            productIds.add(node.get("productId").asLong());
            validateUnitRange(node.get("x"), fieldName, "x");
            validateUnitRange(node.get("y"), fieldName, "y");
        }
        return productIds;
    }

    private void validateUnitRange(JsonNode node, String fieldName, String axis) {
        if (node == null || node.isNull()) {
            return;
        }
        if (!node.isNumber()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, fieldName + "." + axis + " must be numeric");
        }
        double value = node.asDouble();
        if (value < UNIT_RANGE_MIN || value > UNIT_RANGE_MAX) {
            throw new AppException(ErrorCode.VALIDATION_ERROR,
                    fieldName + "." + axis + " must be within [" + UNIT_RANGE_MIN + "," + UNIT_RANGE_MAX + "]");
        }
    }

    private AltarDesignSummaryResponse toSummaryResponse(AltarDesignEntity entity) {
        AltarModelSizeEntity size = entity.getAltarModelSize();
        AltarStyleEntity style = entity.getAltarStyle();
        return AltarDesignSummaryResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .thumb(entity.getThumb())
                .altarModelSizeId(size.getId())
                .altarModelSizeLabel(size.getLabel())
                .altarModelName(size.getAltarModel().getName())
                .altarStyleId(style != null ? style.getId() : null)
                .altarStyleName(style != null ? style.getName() : null)
                .totalPrice(entity.getTotalPrice())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Builds the detail response: maps the persisted scalars, then recomputes prices and drops
     * missing/unpublished-product entries from {@code items}/{@code accessories} against the live
     * catalog (never mutates the stored row — this is a read-time projection only).
     */
    private AltarDesignResponse toDetailResponse(AltarDesignEntity entity) {
        AltarDesignResponse response = altarDesignMapper.toResponse(entity);
        response.setAltarModelSizeLabel(entity.getAltarModelSize().getLabel());
        response.setAltarStyleId(entity.getAltarStyle() != null ? entity.getAltarStyle().getId() : null);

        RecomputeResult recompute = recompute(entity.getItems(), entity.getAccessories());
        response.setItems(recompute.itemsJson());
        response.setAccessories(recompute.accessoriesJson());
        response.setCurrentTotalPrice(recompute.currentTotalPrice());
        response.setDroppedItemCount(recompute.droppedCount());
        return response;
    }

    private record RecomputeResult(String itemsJson, String accessoriesJson, long currentTotalPrice,
                                    int droppedCount) {
    }

    private RecomputeResult recompute(String itemsJson, String accessoriesJson) {
        ArrayNode itemsRoot = parseArrayOrEmpty(itemsJson);
        ArrayNode accessoriesRoot = parseArrayOrEmpty(accessoriesJson);

        Set<Long> productIds = new HashSet<>();
        collectProductIds(itemsRoot, productIds);
        collectProductIds(accessoriesRoot, productIds);

        Map<Long, ProductEntity> livePublished = productIds.isEmpty()
                ? Map.of()
                : productRepository.findAllById(productIds).stream()
                        .filter(product -> product.getStatus() == ProductStatus.PUBLISHED)
                        .collect(Collectors.toMap(ProductEntity::getId, product -> product));

        AtomicInteger dropped = new AtomicInteger();
        AtomicLong total = new AtomicLong();
        ArrayNode filteredItems = filterAndSum(itemsRoot, livePublished, dropped, total);
        ArrayNode filteredAccessories = filterAndSum(accessoriesRoot, livePublished, dropped, total);

        return new RecomputeResult(
                writeJson(filteredItems), writeJson(filteredAccessories), total.get(), dropped.get());
    }

    private ArrayNode parseArrayOrEmpty(String json) {
        if (json == null || json.isBlank()) {
            return objectMapper.createArrayNode();
        }
        try {
            JsonNode node = objectMapper.readTree(json);
            return node.isArray() ? (ArrayNode) node : objectMapper.createArrayNode();
        } catch (Exception e) {
            return objectMapper.createArrayNode();
        }
    }

    private void collectProductIds(ArrayNode arrayNode, Set<Long> ids) {
        for (JsonNode node : arrayNode) {
            if (node.hasNonNull("productId")) {
                ids.add(node.get("productId").asLong());
            }
        }
    }

    private ArrayNode filterAndSum(ArrayNode arrayNode, Map<Long, ProductEntity> livePublished,
                                    AtomicInteger dropped, AtomicLong total) {
        ArrayNode result = objectMapper.createArrayNode();
        for (JsonNode node : arrayNode) {
            Long productId = node.hasNonNull("productId") ? node.get("productId").asLong() : null;
            ProductEntity product = productId != null ? livePublished.get(productId) : null;
            if (product == null) {
                dropped.incrementAndGet();
                continue;
            }
            int quantity = node.hasNonNull("quantity") ? node.get("quantity").asInt() : 1;
            total.addAndGet(product.getPrice() * quantity);
            result.add(node);
        }
        return result;
    }

    private String writeJson(ArrayNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            throw new AppException(ErrorCode.UNCATEGORIZED, "Failed to serialize design payload");
        }
    }

    private AltarModelSizeEntity findAltarModelSizeOrThrow(Long id) {
        return altarModelSizeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ALTAR_MODEL_SIZE_NOT_FOUND));
    }

    private AltarStyleEntity resolveAltarStyle(Long id) {
        if (id == null) {
            return null;
        }
        return altarStyleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ALTAR_STYLE_NOT_FOUND));
    }

    private UserEntity currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails principal)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return principal.getUser();
    }
}
