package vn.springboot.dto.response.media;

import vn.springboot.common.storage.StorageUrl;

/**
 * Result of a file upload: the public URL to persist on the owning entity.
 */
public record UploadResponse(@StorageUrl String url) {
}
