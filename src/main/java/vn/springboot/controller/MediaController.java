package vn.springboot.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vn.springboot.common.response.ApiResponse;
import vn.springboot.dto.response.media.UploadResponse;
import vn.springboot.service.FileStorageService;

import java.util.List;

/**
 * Generic image upload for admin-managed media (product thumbnails, SEO images,
 * gallery images picked in a form…). Returns public URL(s) to store on the owning
 * entity. Gallery images of an existing product can also be managed one-by-one
 * under {@code /api/products/{id}/images}.
 */
@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private static final String DEFAULT_FOLDER = "misc";

    private final FileStorageService fileStorageService;

    /** Upload a single image. */
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<UploadResponse> upload(@RequestParam("file") MultipartFile file,
                                              @RequestParam(value = "folder", required = false) String folder) {
        String url = fileStorageService.uploadImage(file, folder == null ? DEFAULT_FOLDER : folder);
        return ApiResponse.success("Uploaded successfully", new UploadResponse(url));
    }

    /**
     * Upload many images in one request (FE multi-select). Send repeated
     * {@code files} parts in a single {@code multipart/form-data} body; returns the
     * URLs in the same order. All files are validated before any is written.
     */
    @PostMapping(value = "/upload-multiple", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<List<UploadResponse>> uploadMultiple(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "folder", required = false) String folder) {
        List<UploadResponse> data = fileStorageService
                .uploadImages(files, folder == null ? DEFAULT_FOLDER : folder)
                .stream()
                .map(UploadResponse::new)
                .toList();
        return ApiResponse.success("Uploaded successfully", data);
    }
}
