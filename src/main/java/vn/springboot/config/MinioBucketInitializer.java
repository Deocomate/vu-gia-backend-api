package vn.springboot.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Ensures the configured MinIO buckets exist and are anonymously readable on
 * startup — including buckets created by hand in the console (whose policy the
 * on-demand uploader in {@code MinioStorageService} would never touch, since it
 * only sets a policy when it creates the bucket itself).
 *
 * <p>Best-effort: if MinIO is unreachable the failure is logged and startup
 * continues, because the app does not need MinIO to boot or seed.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MinioBucketInitializer implements ApplicationRunner {

    private final MinioClient minioClient;
    private final MinioProperties properties;

    @Override
    public void run(ApplicationArguments args) {
        for (String bucket : List.of(properties.getBucket().getAsset(), properties.getBucket().getProduct())) {
            ensurePublicBucket(bucket);
        }
    }

    private void ensurePublicBucket(String bucket) {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("Created MinIO bucket '{}'", bucket);
            }
            // Always (re)apply the public-read policy — idempotent, and this is what
            // fixes a hand-created bucket that was left private (AccessDenied on GET).
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                    .bucket(bucket)
                    .config(publicReadPolicy(bucket))
                    .build());
            log.info("MinIO bucket '{}' is ready (public-read)", bucket);
        } catch (Exception ex) {
            log.warn("Skipped MinIO bucket init for '{}' (is MinIO reachable?): {}", bucket, ex.getMessage());
        }
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
