package vn.springboot.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.springboot.dto.response.news.NewsCategoryBriefResponse;
import vn.springboot.dto.response.news.NewsResponse;
import vn.springboot.entity.news.NewsCategoryEntity;
import vn.springboot.entity.news.NewsEntity;

/**
 * Maps news entities to their API representations. MapStruct generates the
 * Spring bean at compile time.
 */
@Mapper(componentModel = "spring")
public interface NewsMapper {

    @Mapping(target = "category", source = "newsCategory")
    NewsResponse toResponse(NewsEntity entity);

    NewsCategoryBriefResponse toBrief(NewsCategoryEntity entity);
}
