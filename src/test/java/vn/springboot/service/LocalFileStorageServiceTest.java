package vn.springboot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import vn.springboot.common.exception.AppException;
import vn.springboot.common.exception.ErrorCode;
import vn.springboot.config.StorageProperties;
import vn.springboot.service.impl.LocalFileStorageService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalFileStorageServiceTest {

    @TempDir
    Path tmp;

    private LocalFileStorageService service;

    @BeforeEach
    void setUp() {
        StorageProperties props = new StorageProperties();
        props.setRoot(tmp.toString());
        props.setPublicUrl("http://localhost:8080");
        props.setUrlPrefix("/files");
        service = new LocalFileStorageService(props);
    }

    private MockMultipartFile image(String name, String contentType) {
        return new MockMultipartFile("file", name, contentType, new byte[]{1, 2, 3});
    }

    @Test
    void uploadImage_writesFileAndReturnsRelativeUrl() throws IOException {
        String url = service.uploadImage(image("a.jpg", "image/jpeg"), "products");

        assertThat(url).startsWith("/files/products/").endsWith(".jpg");
        assertThat(url).doesNotContain("localhost");

        Path onDisk = tmp.resolve(url.substring("/files/".length()));
        assertThat(Files.exists(onDisk)).isTrue();
    }

    @Test
    void uploadImages_returnsUrlsInOrder() {
        List<String> urls = service.uploadImages(
                List.of(image("a.png", "image/png"), image("b.webp", "image/webp")), "gallery");

        assertThat(urls).hasSize(2);
        assertThat(urls.get(0)).endsWith(".png");
        assertThat(urls.get(1)).endsWith(".webp");
    }

    @Test
    void uploadImages_oneInvalidFile_rejectsWholeBatchWithoutWritingAny() {
        MockMultipartFile bad = new MockMultipartFile("file", "evil.txt", "text/plain", new byte[]{1});

        assertThatThrownBy(() -> service.uploadImages(List.of(image("a.jpg", "image/jpeg"), bad), "gallery"))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.INVALID_FILE));

        try (Stream<Path> written = Files.walk(tmp)) {
            assertThat(written.filter(Files::isRegularFile)).isEmpty();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void delete_removesFile() {
        String url = service.uploadImage(image("a.jpg", "image/jpeg"), "products");
        Path onDisk = tmp.resolve(url.substring("/files/".length()));
        assertThat(Files.exists(onDisk)).isTrue();

        service.delete(url);

        assertThat(Files.exists(onDisk)).isFalse();
    }

    @Test
    void delete_unrelatedUrl_isNoOpAndDoesNotThrow() {
        service.delete("assets/images/products/keep-me.jpg");
        service.delete((String) null);
        service.delete("");
        // no exception = pass
    }

    @Test
    void validate_rejectsEmptyOrNonImageFile() {
        assertThatThrownBy(() -> service.uploadImage(
                new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]), "products"))
                .isInstanceOf(AppException.class);

        assertThatThrownBy(() -> service.uploadImage(image("a.txt", "text/plain"), "products"))
                .isInstanceOf(AppException.class);
    }

    @Test
    void uploadImage_pathTraversalInFolder_isRejected() {
        assertThatThrownBy(() -> service.uploadImage(image("a.jpg", "image/jpeg"), "../../evil"))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.INVALID_FILE));

        try (Stream<Path> written = Files.walk(tmp)) {
            assertThat(written.filter(Files::isRegularFile)).isEmpty();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void uploadImage_spoofedHtmlFile_isStoredWithImageExtensionNotHtml() throws IOException {
        // Attacker names the file evil.html but spoofs the multipart Content-Type as image/png.
        String url = service.uploadImage(image("evil.html", "image/png"), "products");

        assertThat(url).endsWith(".png");
        assertThat(url).doesNotEndWith(".html");
    }

    @Test
    void uploadImage_svgIsRejected() {
        assertThatThrownBy(() -> service.uploadImage(image("evil.svg", "image/svg+xml"), "products"))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.INVALID_FILE));
    }
}
