package vn.springboot.common.storage;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.springboot.config.StorageProperties;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

/** Unit tests for the {@code @StorageUrl} serializer, invoked directly (no Spring context). */
class StorageUrlSerializerTest {

    private StorageUrlSerializer serializer;
    private JsonFactory jsonFactory;

    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        StorageProperties props = new StorageProperties();
        props.setPublicUrl("http://localhost:8080");
        props.setUrlPrefix("/files");

        serializer = new StorageUrlSerializer();
        Field field = StorageUrlSerializer.class.getDeclaredField("properties");
        field.setAccessible(true);
        field.set(serializer, props);

        jsonFactory = new JsonFactory();
    }

    private String write(String value) throws IOException {
        StringWriter out = new StringWriter();
        try (JsonGenerator gen = jsonFactory.createGenerator(out)) {
            serializer.serialize(value, gen, null);
        }
        return out.toString();
    }

    @Test
    void relativeStoragePath_isPrependedWithPublicBase() throws IOException {
        assertThat(write("/files/x.jpg")).isEqualTo("\"http://localhost:8080/files/x.jpg\"");
    }

    @Test
    void seedAssetPath_isLeftUnchanged() throws IOException {
        assertThat(write("assets/images/y.png")).isEqualTo("\"assets/images/y.png\"");
    }

    @Test
    void externalUrl_isLeftUnchanged() throws IOException {
        assertThat(write("https://cdn.ext/z.jpg")).isEqualTo("\"https://cdn.ext/z.jpg\"");
    }

    @Test
    void nullValue_isSerializedAsNull() throws IOException {
        assertThat(write(null)).isEqualTo("null");
    }
}
