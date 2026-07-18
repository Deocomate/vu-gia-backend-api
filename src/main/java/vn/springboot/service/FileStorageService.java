package vn.springboot.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Object storage abstraction (backed by the local filesystem under
 * {@code app.storage.root}). Uploads are returned as relative paths, made
 * absolute at the JSON boundary via {@code @StorageUrl}.
 */
public interface FileStorageService {

    /**
     * Upload an image file under {@code folder} and return its public URL.
     * Validates that the file is a non-empty image; throws
     * {@link vn.springboot.common.exception.AppException} otherwise.
     *
     * @param folder logical prefix inside the bucket (e.g. {@code "products"}), may be null/blank
     */
    String uploadImage(MultipartFile file, String folder);

    /**
     * Upload several image files in one call and return their public URLs (in order).
     * All files are validated up-front, so a single invalid file rejects the whole
     * batch before anything is written (no partial uploads).
     */
    List<String> uploadImages(List<MultipartFile> files, String folder);

    /** Best-effort delete of a previously uploaded object, given its public URL. */
    void delete(String url);
}
