package vn.springboot.common.storage;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a DTO field that holds a storage-managed image URL. The DB/entity value
 * is always a relative path ({@code /files/...}); this annotation makes the JSON
 * boundary transparently absolute so the frontend never has to change:
 *
 * <ul>
 *   <li>Serialize (response): {@code /files/x.jpg} -> {@code http://host/files/x.jpg}</li>
 *   <li>Deserialize (request): {@code http://host/files/x.jpg} -> {@code /files/x.jpg}</li>
 * </ul>
 *
 * Values that are not storage-managed (external URLs, seed {@code assets/...}
 * paths, null) pass through unchanged in both directions.
 */
@JacksonAnnotationsInside
@JsonSerialize(using = StorageUrlSerializer.class)
@JsonDeserialize(using = StorageUrlDeserializer.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface StorageUrl {
}
