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
import vn.springboot.dto.request.newsletter.NewsletterSubscribeRequest;
import vn.springboot.dto.request.newsletter.NewsletterSubscriberSearchRequest;
import vn.springboot.dto.request.newsletter.NewsletterSubscriberUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.newsletter.NewsletterSubscriberResponse;
import vn.springboot.service.NewsletterSubscriberService;

/**
 * Newsletter subscription endpoints. Subscribing is public; managing subscribers
 * requires staff roles ({@code ADMIN} / {@code SUPERADMIN}).
 */
@RestController
@RequestMapping("/api/newsletter-subscribers")
@RequiredArgsConstructor
public class NewsletterSubscriberController {

    private final NewsletterSubscriberService subscriberService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<PageResponse<NewsletterSubscriberResponse>> search(
            @ModelAttribute NewsletterSubscriberSearchRequest request) {
        return ApiResponse.success(subscriberService.search(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<NewsletterSubscriberResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(subscriberService.getById(id));
    }

    @PostMapping
    public ApiResponse<NewsletterSubscriberResponse> subscribe(
            @Valid @RequestBody NewsletterSubscribeRequest request) {
        return ApiResponse.success("Subscribed successfully", subscriberService.subscribe(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<NewsletterSubscriberResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody NewsletterSubscriberUpdateRequest request) {
        return ApiResponse.success("Updated successfully", subscriberService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        subscriberService.delete(id);
        return ApiResponse.success("Deleted successfully", null);
    }
}
