package vn.springboot.common.storage;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import vn.springboot.config.StorageProperties;

import java.io.IOException;

/**
 * Strips {@code app.storage.public-url + url-prefix} back to a relative path on
 * the way in, so the DB always stores a domain-independent value. Only strips
 * when the value actually starts with our own base+prefix (or bare prefix) —
 * external URLs (CDN, OAuth avatar…) and seed {@code assets/...} paths are left
 * untouched.
 */
public class StorageUrlDeserializer extends StdDeserializer<String> {

    @Autowired
    private StorageProperties properties;

    public StorageUrlDeserializer() {
        super(String.class);
    }

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();
        if (value == null) {
            return null;
        }
        String absolutePrefix = normalizedBase() + normalizedPrefix();
        if (value.startsWith(absolutePrefix)) {
            return value.substring(normalizedBase().length());
        }
        return value;
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
