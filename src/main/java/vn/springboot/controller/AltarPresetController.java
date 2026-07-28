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
import vn.springboot.dto.request.altar.AltarPresetRequest;
import vn.springboot.dto.request.altar.AltarPresetSearchRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.altar.AltarPresetResponse;
import vn.springboot.service.AltarPresetService;

/**
 * Suggested-preset ("bộ gợi ý") endpoints. Reads are public (storefront preset chooser + admin
 * builder list share the same endpoint — {@code isActive} defaults to {@code true}, see
 * {@link AltarPresetSearchRequest}); writes require staff roles ({@code ADMIN} / {@code SUPERADMIN}).
 */
@RestController
@RequestMapping("/api/altar-presets")
@RequiredArgsConstructor
public class AltarPresetController {

    private final AltarPresetService altarPresetService;

    @GetMapping
    public ApiResponse<PageResponse<AltarPresetResponse>> search(
            @ModelAttribute AltarPresetSearchRequest request) {
        return ApiResponse.success(altarPresetService.search(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<AltarPresetResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(altarPresetService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<AltarPresetResponse> create(@Valid @RequestBody AltarPresetRequest request) {
        return ApiResponse.success("Created successfully", altarPresetService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<AltarPresetResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AltarPresetRequest request) {
        return ApiResponse.success("Updated successfully", altarPresetService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        altarPresetService.delete(id);
        return ApiResponse.success("Deleted successfully", null);
    }
}
