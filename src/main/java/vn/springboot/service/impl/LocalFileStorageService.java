package vn.springboot.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.springboot.common.exception.AppException;
import vn.springboot.common.exception.ErrorCode;
import vn.springboot.config.StorageProperties;
import vn.springboot.service.FileStorageService;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Local-filesystem-backed {@link FileStorageService}. Files are written under
 * {@code app.storage.root} and returned as a relative path
 * ({@code {url-prefix}/{objectName}}) so the persisted value is
 * deployment-independent; {@code @StorageUrl} resolves it to an absolute URL
 * at the JSON boundary.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocalFileStorageService implements FileStorageService {

    // Extension derived from the VALIDATED content-type — never from the original
    // filename — so an attacker cannot smuggle an .html/.svg object onto the
    // backend's own origin by spoofing the multipart Content-Type header.
    private static final Map<String, String> ALLOWED_CONTENT_TYPES = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp",
            "image/gif", ".gif"
    );

    private final StorageProperties properties;

    @PostConstruct
    void logPublicBase() {
        String base = publicBase();
        log.info("File storage public base: {}", base);
        if (base.contains("localhost") && !base.contains("localhost:8080")) {
            log.warn("File storage public base looks like a dev default ({}); set APP_STORAGE_PUBLIC_URL in production", base);
        }
    }

    @Override
    public String uploadImage(MultipartFile file, String folder) {
        String ext = validateAndResolveExtension(file);

        Path base = resolveBase();
        String objectName = buildObjectName(folder, ext);
        Path target = base.resolve(objectName).normalize();
        if (!target.startsWith(base)) {
            throw new AppException(ErrorCode.INVALID_FILE);
        }

        try (InputStream in = file.getInputStream()) {
            Files.createDirectories(target.getParent());
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception ex) {
            log.error("Failed to write file '{}'", target, ex);
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        return urlPrefix() + "/" + objectName;
    }

    @Override
    public List<String> uploadImages(List<MultipartFile> files, String folder) {
        if (files == null || files.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_FILE);
        }
        // Validate every file first so one bad file rejects the whole batch (no partial uploads).
        files.forEach(this::validateAndResolveExtension);
        return files.stream().map(file -> uploadImage(file, folder)).toList();
    }

    @Override
    public void delete(String url) {
        if (url == null || url.isBlank()) {
            return;
        }
        String prefix = urlPrefix() + "/";
        String relative = url;
        String absolutePrefix = publicBase() + prefix;
        if (relative.startsWith(absolutePrefix)) {
            relative = relative.substring(publicBase().length());
        }
        if (!relative.startsWith(prefix)) {
            return; // not managed by this storage (e.g. seed's assets/... path)
        }

        Path base = resolveBase();
        String objectName = relative.substring(prefix.length());
        Path target = base.resolve(objectName).normalize();
        if (!target.startsWith(base)) {
            return;
        }
        try {
            Files.deleteIfExists(target);
        } catch (Exception ex) {
            // Deletion is best-effort; a dangling file must not fail the request.
            log.warn("Failed to delete file '{}': {}", target, ex.getMessage());
        }
    }

    private String validateAndResolveExtension(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_FILE);
        }
        String contentType = file.getContentType();
        String ext = contentType == null ? null : ALLOWED_CONTENT_TYPES.get(contentType.toLowerCase());
        if (ext == null) {
            throw new AppException(ErrorCode.INVALID_FILE);
        }
        return ext;
    }

    private String buildObjectName(String folder, String ext) {
        String name = UUID.randomUUID().toString().replace("-", "") + ext;
        return (folder == null || folder.isBlank()) ? name : folder + "/" + name;
    }

    private Path resolveBase() {
        return Paths.get(properties.getRoot()).toAbsolutePath().normalize();
    }

    private String urlPrefix() {
        String prefix = properties.getUrlPrefix();
        return prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) : prefix;
    }

    private String publicBase() {
        String base = properties.getPublicUrl();
        return base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    }
}
