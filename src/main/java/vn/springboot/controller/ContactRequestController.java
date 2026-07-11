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
import vn.springboot.dto.request.contact.ContactRequestCreateRequest;
import vn.springboot.dto.request.contact.ContactRequestSearchRequest;
import vn.springboot.dto.request.contact.ContactRequestUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.contact.ContactRequestResponse;
import vn.springboot.service.ContactRequestService;

/**
 * Contact-form endpoints. Submitting is public; reviewing and managing requests
 * requires staff roles ({@code ADMIN} / {@code SUPERADMIN}).
 */
@RestController
@RequestMapping("/api/contact-requests")
@RequiredArgsConstructor
public class ContactRequestController {

    private final ContactRequestService contactRequestService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<PageResponse<ContactRequestResponse>> search(
            @ModelAttribute ContactRequestSearchRequest request) {
        return ApiResponse.success(contactRequestService.search(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<ContactRequestResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(contactRequestService.getById(id));
    }

    @PostMapping
    public ApiResponse<ContactRequestResponse> create(
            @Valid @RequestBody ContactRequestCreateRequest request) {
        return ApiResponse.success("Submitted successfully", contactRequestService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<ContactRequestResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ContactRequestUpdateRequest request) {
        return ApiResponse.success("Updated successfully", contactRequestService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        contactRequestService.delete(id);
        return ApiResponse.success("Deleted successfully", null);
    }
}
