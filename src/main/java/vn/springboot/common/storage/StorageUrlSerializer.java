package vn.springboot.common.storage;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import vn.springboot.config.StorageProperties;

import java.io.IOException;

/**
 * Prepends {@code app.storage.public-url} to relative {@code url-prefix} paths on
 * the way out. Values that don't start with {@code url-prefix} (external URLs,
 * seed {@code assets/...} paths) are written unchanged.
 */
public class StorageUrlSerializer extends StdSerializer<String> {

    @Autowired
    private StorageProperties properties;

    public StorageUrlSerializer() {
        super(String.class);
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        String prefix = normalizedPrefix();
        if (!value.startsWith(prefix)) {
            gen.writeString(value);
            return;
        }
        gen.writeString(normalizedBase() + value);
    }

    private String normalizedPrefix() {
        String prefix = properties.getUrlPrefix();
        return prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) : prefix;
    }

    private String normalizedBase() {
        String base = properties.getPublicUrl();
        return base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    }
}
