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
import vn.springboot.dto.request.gallery.GalleryImageCreateRequest;
import vn.springboot.dto.request.gallery.GalleryImageSearchRequest;
import vn.springboot.dto.request.gallery.GalleryImageUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.gallery.GalleryImageResponse;
import vn.springboot.service.GalleryImageService;

/**
 * Gallery image endpoints. Reads are public; writes require staff roles
 * ({@code ADMIN} / {@code SUPERADMIN}).
 */
@RestController
@RequestMapping("/api/gallery-images")
@RequiredArgsConstructor
public class GalleryImageController {

    private final GalleryImageService galleryImageService;

    @GetMapping
    public ApiResponse<PageResponse<GalleryImageResponse>> search(
            @ModelAttribute GalleryImageSearchRequest request) {
        return ApiResponse.success(galleryImageService.search(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<GalleryImageResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(galleryImageService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<GalleryImageResponse> create(
            @Valid @RequestBody GalleryImageCreateRequest request) {
        return ApiResponse.success("Created successfully", galleryImageService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<GalleryImageResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody GalleryImageUpdateRequest request) {
        return ApiResponse.success("Updated successfully", galleryImageService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        galleryImageService.delete(id);
        return ApiResponse.success("Deleted successfully", null);
    }
}
