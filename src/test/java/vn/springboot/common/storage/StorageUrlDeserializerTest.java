package vn.springboot.common.storage;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.springboot.config.StorageProperties;

import java.io.IOException;
import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

/** Unit tests for the {@code @StorageUrl} deserializer, invoked directly (no Spring context). */
class StorageUrlDeserializerTest {

    private StorageUrlDeserializer deserializer;
    private JsonFactory jsonFactory;

    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        StorageProperties props = new StorageProperties();
        props.setPublicUrl("http://localhost:8080");
        props.setUrlPrefix("/files");

        deserializer = new StorageUrlDeserializer();
        Field field = StorageUrlDeserializer.class.getDeclaredField("properties");
        field.setAccessible(true);
        field.set(deserializer, props);

        jsonFactory = new JsonFactory();
    }

    private String read(String json) throws IOException {
        try (JsonParser parser = jsonFactory.createParser(json)) {
            parser.nextToken();
            assertThat(parser.currentToken()).isIn(JsonToken.VALUE_STRING, JsonToken.VALUE_NULL);
            return deserializer.deserialize(parser, null);
        }
    }

    @Test
    void absoluteStorageUrl_isStrippedToRelativePath() throws IOException {
        assertThat(read("\"http://localhost:8080/files/x.jpg\"")).isEqualTo("/files/x.jpg");
    }

    @Test
    void alreadyRelativePath_isLeftUnchanged() throws IOException {
        assertThat(read("\"/files/x.jpg\"")).isEqualTo("/files/x.jpg");
    }

    @Test
    void seedAssetPath_isLeftUnchanged() throws IOException {
        assertThat(read("\"assets/images/y.png\"")).isEqualTo("assets/images/y.png");
    }

    @Test
    void externalUrl_isLeftUnchanged() throws IOException {
        assertThat(read("\"https://cdn.ext/z.jpg\"")).isEqualTo("https://cdn.ext/z.jpg");
    }

    @Test
    void nullValue_isLeftNull() throws IOException {
        assertThat(read("null")).isNull();
    }
}
