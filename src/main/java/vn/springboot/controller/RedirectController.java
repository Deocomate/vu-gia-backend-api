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
import vn.springboot.dto.request.redirect.RedirectCreateRequest;
import vn.springboot.dto.request.redirect.RedirectSearchRequest;
import vn.springboot.dto.request.redirect.RedirectUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.redirect.RedirectResponse;
import vn.springboot.service.RedirectService;

/**
 * Redirect endpoints. Reads are public; writes require staff roles
 * ({@code ADMIN} / {@code SUPERADMIN}).
 */
@RestController
@RequestMapping("/api/redirects")
@RequiredArgsConstructor
public class RedirectController {

    private final RedirectService redirectService;

    @GetMapping
    public ApiResponse<PageResponse<RedirectResponse>> search(
            @ModelAttribute RedirectSearchRequest request) {
        return ApiResponse.success(redirectService.search(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<RedirectResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(redirectService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<RedirectResponse> create(
            @Valid @RequestBody RedirectCreateRequest request) {
        return ApiResponse.success("Created successfully", redirectService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<RedirectResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody RedirectUpdateRequest request) {
        return ApiResponse.success("Updated successfully", redirectService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        redirectService.delete(id);
        return ApiResponse.success("Deleted successfully", null);
    }
}
