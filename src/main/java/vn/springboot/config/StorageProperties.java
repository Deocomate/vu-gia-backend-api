package vn.springboot.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds {@code app.storage.*} — where uploaded files live on disk and how their
 * public URL is built (public base + prefix + relative path stored in DB).
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {

    /** Root directory on disk where files are written (relative or absolute). */
    private String root = "./data";

    /** Public base URL prepended to {@code urlPrefix} when serializing responses. */
    private String publicUrl = "http://localhost:8080";

    /** Path under which files are served, e.g. {@code /files}. */
    private String urlPrefix = "/files";
}
