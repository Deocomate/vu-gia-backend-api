package vn.springboot.service.impl;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.SetBucketPolicyArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.springboot.common.exception.AppException;
import vn.springboot.common.exception.ErrorCode;
import vn.springboot.config.MinioProperties;
import vn.springboot.service.FileStorageService;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

/**
 * MinIO-backed {@link FileStorageService}. Buckets are created on demand and
 * made anonymously readable so the returned URLs are directly usable by clients.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinioStorageService implements FileStorageService {

    private final MinioClient minioClient;
    private final MinioProperties properties;

    @Override
    public String uploadImage(MultipartFile file, String folder) {
        validateImage(file);

        String bucket = properties.getBucket().getProduct();
        ensureBucket(bucket);

        String objectName = buildObjectName(folder, file.getOriginalFilename());
        try (InputStream in = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(in, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
        } catch (Exception ex) {
            log.error("Failed to upload object '{}' to bucket '{}'", objectName, bucket, ex);
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        return publicBase() + "/" + bucket + "/" + objectName;
    }

    @Override
    public List<String> uploadImages(List<MultipartFile> files, String folder) {
        if (files == null || files.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_FILE);
        }
        // Validate every file first so one bad file rejects the whole batch (no partial uploads).
        files.forEach(this::validateImage);
        return files.stream().map(file -> uploadImage(file, folder)).toList();
    }

    @Override
    public void delete(String url) {
        if (url == null || url.isBlank()) {
            return;
        }
        String bucket = properties.getBucket().getProduct();
        String prefix = publicBase() + "/" + bucket + "/";
        if (!url.startsWith(prefix)) {
            return; // not one of ours; leave it alone
        }
        String objectName = url.substring(prefix.length());
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
        } catch (Exception ex) {
            // Deletion is best-effort; a dangling object must not fail the request.
            log.warn("Failed to delete object '{}' from bucket '{}': {}", objectName, bucket, ex.getMessage());
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_FILE);
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new AppException(ErrorCode.INVALID_FILE);
        }
    }

    private void ensureBucket(String bucket) {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                        .bucket(bucket)
                        .config(publicReadPolicy(bucket))
                        .build());
            }
        } catch (Exception ex) {
            log.error("Failed to ensure bucket '{}'", bucket, ex);
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    private String buildObjectName(String folder, String originalName) {
        String ext = "";
        if (originalName != null) {
            int dot = originalName.lastIndexOf('.');
            if (dot >= 0 && dot < originalName.length() - 1) {
                ext = originalName.substring(dot).toLowerCase();
            }
        }
        String name = UUID.randomUUID().toString().replace("-", "") + ext;
        return (folder == null || folder.isBlank()) ? name : folder + "/" + name;
    }

    private String publicBase() {
        String base = properties.getPublicUrl();
        if (base == null || base.isBlank()) {
            base = properties.getUrl();
        }
        return base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    }

    private String publicReadPolicy(String bucket) {
        return """
                {
                  "Version":"2012-10-17",
                  "Statement":[
                    {
                      "Effect":"Allow",
                      "Principal":"*",
                      "Action":["s3:GetObject"],
                      "Resource":["arn:aws:s3:::%s/*"]
                    }
                  ]
                }
                """.formatted(bucket);
    }
}
