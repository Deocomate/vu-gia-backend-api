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
import vn.springboot.dto.request.faq.FaqCreateRequest;
import vn.springboot.dto.request.faq.FaqSearchRequest;
import vn.springboot.dto.request.faq.FaqUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.faq.FaqResponse;
import vn.springboot.service.FaqService;

/**
 * FAQ endpoints. Reads are public; writes require staff roles
 * ({@code ADMIN} / {@code SUPERADMIN}).
 */
@RestController
@RequestMapping("/api/faqs")
@RequiredArgsConstructor
public class FaqController {

    private final FaqService faqService;

    @GetMapping
    public ApiResponse<PageResponse<FaqResponse>> search(
            @ModelAttribute FaqSearchRequest request) {
        return ApiResponse.success(faqService.search(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<FaqResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(faqService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<FaqResponse> create(
            @Valid @RequestBody FaqCreateRequest request) {
        return ApiResponse.success("Created successfully", faqService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<FaqResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody FaqUpdateRequest request) {
        return ApiResponse.success("Updated successfully", faqService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        faqService.delete(id);
        return ApiResponse.success("Deleted successfully", null);
    }
}
