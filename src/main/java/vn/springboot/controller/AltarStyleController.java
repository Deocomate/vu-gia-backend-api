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
import vn.springboot.dto.request.altar.AltarStyleCreateRequest;
import vn.springboot.dto.request.altar.AltarStyleSearchRequest;
import vn.springboot.dto.request.altar.AltarStyleUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.altar.AltarStyleResponse;
import vn.springboot.service.AltarStyleService;

/**
 * Altar glaze/finish style endpoints (Men lam, Men rạn, ...). Reads are public (storefront
 * customizer); writes require staff roles ({@code ADMIN} / {@code SUPERADMIN}).
 */
@RestController
@RequestMapping("/api/altar-styles")
@RequiredArgsConstructor
public class AltarStyleController {

    private final AltarStyleService altarStyleService;

    @GetMapping
    public ApiResponse<PageResponse<AltarStyleResponse>> search(
            @ModelAttribute AltarStyleSearchRequest request) {
        return ApiResponse.success(altarStyleService.search(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<AltarStyleResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(altarStyleService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<AltarStyleResponse> create(@Valid @RequestBody AltarStyleCreateRequest request) {
        return ApiResponse.success("Created successfully", altarStyleService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<AltarStyleResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AltarStyleUpdateRequest request) {
        return ApiResponse.success("Updated successfully", altarStyleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        altarStyleService.delete(id);
        return ApiResponse.success("Deleted successfully", null);
    }
}
