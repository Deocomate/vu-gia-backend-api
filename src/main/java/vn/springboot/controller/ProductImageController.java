package vn.springboot.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vn.springboot.common.response.ApiResponse;
import vn.springboot.dto.response.product.ProductImageResponse;
import vn.springboot.service.ProductImageService;

import java.util.List;

/**
 * Product image gallery endpoints. Reads are public; writes require staff roles
 * ({@code ADMIN} / {@code SUPERADMIN}). Uploads land on the local filesystem.
 */
@RestController
@RequestMapping("/api/products/{productId}/images")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageService productImageService;

    @GetMapping
    public ApiResponse<List<ProductImageResponse>> listImages(@PathVariable Long productId) {
        return ApiResponse.success(productImageService.listImages(productId));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<ProductImageResponse> addImage(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "priority", required = false) Integer priority) {
        return ApiResponse.success("Uploaded successfully",
                productImageService.addImage(productId, file, priority));
    }

    @PatchMapping("/{imageId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<ProductImageResponse> updatePriority(
            @PathVariable Long productId,
            @PathVariable Long imageId,
            @RequestParam Integer priority) {
        return ApiResponse.success("Updated successfully",
                productImageService.updatePriority(productId, imageId, priority));
    }

    @DeleteMapping("/{imageId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<Void> deleteImage(
            @PathVariable Long productId,
            @PathVariable Long imageId) {
        productImageService.deleteImage(productId, imageId);
        return ApiResponse.success("Deleted successfully", null);
    }
}
