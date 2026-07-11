package vn.springboot.mapper;

import org.mapstruct.Mapper;
import vn.springboot.dto.response.newsletter.NewsletterSubscriberResponse;
import vn.springboot.entity.newsletter.NewsletterSubscriberEntity;

/**
 * Maps {@link NewsletterSubscriberEntity} to its API representation. All exposed
 * fields map by name. MapStruct generates the Spring bean at compile time.
 */
@Mapper(componentModel = "spring")
public interface NewsletterSubscriberMapper {

    NewsletterSubscriberResponse toResponse(NewsletterSubscriberEntity entity);
}
