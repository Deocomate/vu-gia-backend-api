package vn.springboot.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.springboot.common.response.ApiResponse;
import vn.springboot.dto.request.altar.AltarModelCreateRequest;
import vn.springboot.dto.request.altar.AltarModelSearchRequest;
import vn.springboot.dto.request.altar.AltarModelSizeCreateRequest;
import vn.springboot.dto.request.altar.AltarModelSizeUpdateRequest;
import vn.springboot.dto.request.altar.AltarModelUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.altar.AltarModelResponse;
import vn.springboot.dto.response.altar.AltarModelSizeResponse;
import vn.springboot.service.AltarModelService;

/**
 * Altar model endpoints (loại bàn thờ), including the nested per-model size CRUD. Reads are
 * public (storefront customizer); writes require staff roles ({@code ADMIN} / {@code SUPERADMIN}).
 */
@RestController
@RequestMapping("/api/altar-models")
@RequiredArgsConstructor
public class AltarModelController {

    private final AltarModelService altarModelService;

    @GetMapping
    public ApiResponse<PageResponse<AltarModelResponse>> search(
            @ModelAttribute AltarModelSearchRequest request) {
        return ApiResponse.success(altarModelService.search(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<AltarModelResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(altarModelService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<AltarModelResponse> create(@Valid @RequestBody AltarModelCreateRequest request) {
        return ApiResponse.success("Created successfully", altarModelService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<AltarModelResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AltarModelUpdateRequest request) {
        return ApiResponse.success("Updated successfully", altarModelService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        altarModelService.delete(id);
        return ApiResponse.success("Deleted successfully", null);
    }

    @PostMapping("/{modelId}/sizes")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<AltarModelSizeResponse> createSize(
            @PathVariable Long modelId,
            @Valid @RequestBody AltarModelSizeCreateRequest request) {
        return ApiResponse.success("Created successfully", altarModelService.createSize(modelId, request));
    }

    @PutMapping("/{modelId}/sizes/{sizeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<AltarModelSizeResponse> updateSize(
            @PathVariable Long modelId,
            @PathVariable Long sizeId,
            @Valid @RequestBody AltarModelSizeUpdateRequest request) {
        return ApiResponse.success("Updated successfully", altarModelService.updateSize(modelId, sizeId, request));
    }

    @DeleteMapping("/{modelId}/sizes/{sizeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<Void> deleteSize(@PathVariable Long modelId, @PathVariable Long sizeId) {
        altarModelService.deleteSize(modelId, sizeId);
        return ApiResponse.success("Deleted successfully", null);
    }
}
