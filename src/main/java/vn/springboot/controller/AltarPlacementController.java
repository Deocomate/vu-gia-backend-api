package vn.springboot.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.springboot.common.response.ApiResponse;
import vn.springboot.dto.request.altar.AltarPlacementRequest;
import vn.springboot.dto.response.altar.AltarPlacementResponse;
import vn.springboot.service.AltarPlacementService;

/**
 * Per-product-image altar placement (admin drag-drop editor authoring target). Reads are public
 * (already covered by {@code /api/products/**} in {@code SecurityConfig.PUBLIC_GET_ENDPOINTS});
 * writes require staff roles.
 */
@RestController
@RequestMapping("/api/products/{productId}/images/{imageId}/placement")
@RequiredArgsConstructor
public class AltarPlacementController {

    private final AltarPlacementService altarPlacementService;

    @GetMapping
    public ApiResponse<AltarPlacementResponse> get(
            @PathVariable Long productId, @PathVariable Long imageId) {
        return ApiResponse.success(altarPlacementService.getByProductImage(productId, imageId));
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<AltarPlacementResponse> upsert(
            @PathVariable Long productId,
            @PathVariable Long imageId,
            @Valid @RequestBody AltarPlacementRequest request) {
        return ApiResponse.success("Saved successfully", altarPlacementService.upsert(productId, imageId, request));
    }

    @DeleteMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long productId, @PathVariable Long imageId) {
        altarPlacementService.delete(productId, imageId);
        return ApiResponse.success("Deleted successfully", null);
    }
}
