package vn.springboot.dto.response.media;

/**
 * Result of a file upload: the public URL to persist on the owning entity.
 */
public record UploadResponse(String url) {
}
