package vn.springboot.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds the {@code minio.*} configuration block: connection credentials, the
 * public base URL used to build object links, and per-purpose bucket names.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    private String url;
    private String accessKey;
    private String secretKey;

    /** Base URL for building public object links (may be a CDN/proxy in front of MinIO). */
    private String publicUrl;

    private Bucket bucket = new Bucket();

    @Getter
    @Setter
    public static class Bucket {
        /** Bucket for product-domain media uploaded via the app (thumbnails, gallery, seo). */
        private String product = "products";
        /** Bucket for static site/theme assets (logo, icons, patterns, hero images). */
        private String asset = "assets";
    }
}
