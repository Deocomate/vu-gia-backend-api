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
import vn.springboot.dto.request.altar.AltarItemGroupCreateRequest;
import vn.springboot.dto.request.altar.AltarItemGroupSearchRequest;
import vn.springboot.dto.request.altar.AltarItemGroupUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.altar.AltarItemGroupResponse;
import vn.springboot.service.AltarItemGroupService;

/**
 * Altar item group endpoints (Bộ tam sự - ngũ sự, Bát hương &amp; phụ kiện, ...).
 * Reads are public (storefront customizer); writes require staff roles
 * ({@code ADMIN} / {@code SUPERADMIN}).
 */
@RestController
@RequestMapping("/api/altar-item-groups")
@RequiredArgsConstructor
public class AltarItemGroupController {

    private final AltarItemGroupService altarItemGroupService;

    @GetMapping
    public ApiResponse<PageResponse<AltarItemGroupResponse>> search(
            @ModelAttribute AltarItemGroupSearchRequest request) {
        return ApiResponse.success(altarItemGroupService.search(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<AltarItemGroupResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(altarItemGroupService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<AltarItemGroupResponse> create(
            @Valid @RequestBody AltarItemGroupCreateRequest request) {
        return ApiResponse.success("Created successfully", altarItemGroupService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<AltarItemGroupResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AltarItemGroupUpdateRequest request) {
        return ApiResponse.success("Updated successfully", altarItemGroupService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        altarItemGroupService.delete(id);
        return ApiResponse.success("Deleted successfully", null);
    }
}
