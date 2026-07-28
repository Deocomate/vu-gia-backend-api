package vn.springboot.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.springboot.common.response.ApiResponse;
import vn.springboot.dto.request.altar.AltarDesignRenameRequest;
import vn.springboot.dto.request.altar.AltarDesignRequest;
import vn.springboot.dto.request.altar.AltarDesignSearchRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.altar.AltarDesignResponse;
import vn.springboot.dto.response.altar.AltarDesignSummaryResponse;
import vn.springboot.service.AltarDesignService;

/**
 * Per-user saved-design library ("Lưu vào thư viện"). No {@code @PreAuthorize} role restriction —
 * any authenticated account (customer or staff) may have designs, this is a customer feature, not
 * staff-only. Authentication itself is enforced by {@code SecurityConfig}'s default
 * {@code anyRequest().authenticated()} (this path is not listed in
 * {@code PUBLIC_ENDPOINTS}/{@code PUBLIC_GET_ENDPOINTS}). Ownership is enforced entirely in
 * {@code AltarDesignServiceImpl} — the caller is resolved from the security context there, never
 * accepted as a request parameter here.
 */
@RestController
@RequestMapping("/api/altar-designs")
@RequiredArgsConstructor
public class AltarDesignController {

    private final AltarDesignService altarDesignService;

    @GetMapping
    public ApiResponse<PageResponse<AltarDesignSummaryResponse>> list(
            @ModelAttribute AltarDesignSearchRequest request) {
        return ApiResponse.success(altarDesignService.list(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<AltarDesignResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(altarDesignService.getById(id));
    }

    @PostMapping
    public ApiResponse<AltarDesignResponse> create(@Valid @RequestBody AltarDesignRequest request) {
        return ApiResponse.success("Design saved", altarDesignService.create(request));
    }

    @PatchMapping("/{id}")
    public ApiResponse<AltarDesignResponse> rename(
            @PathVariable Long id, @Valid @RequestBody AltarDesignRenameRequest request) {
        return ApiResponse.success("Design renamed", altarDesignService.rename(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        altarDesignService.delete(id);
        return ApiResponse.success("Design deleted", null);
    }
}
