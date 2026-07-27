package vn.springboot.common.storage;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import vn.springboot.config.StorageProperties;

import java.io.IOException;

/**
 * Prepends {@code app.storage.public-url} to relative {@code url-prefix} paths on
 * the way out. Values that don't start with {@code url-prefix} (external URLs,
 * seed {@code assets/...} paths) are written unchanged.
 *
 * <p>Registered via {@code @JsonSerialize(using = ...)} on {@link StorageUrl}, so
 * Jackson (not the regular Spring MVC container) instantiates this class. Spring
 * Boot's auto-configured {@code Jackson2ObjectMapperBuilder} wires its
 * {@code ApplicationContext} into the {@code ObjectMapper}'s
 * {@code HandlerInstantiator} ({@code SpringHandlerInstantiator}), which creates
 * every Jackson-instantiated (de)serializer via
 * {@code AutowireCapableBeanFactory#createBean(Class)}. That factory method
 * autowires the single constructor of a class even without an explicit
 * {@code @Autowired} annotation (Spring's implicit single-constructor
 * autowiring), so constructor injection here is fully within Spring's DI reach —
 * unlike field {@code @Autowired}, which required Spring's AspectJ
 * {@code @Configurable} weaving (not enabled in this project) to work reliably
 * outside container-managed beans.
 */
public class StorageUrlSerializer extends StdSerializer<String> {

    private final StorageProperties properties;

    public StorageUrlSerializer(StorageProperties properties) {
        super(String.class);
        this.properties = properties;
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
