package dev.mkopp.mysite.blog.infrastructure.adapter.out.persistence.mapper;

import dev.mkopp.mysite.blog.domain.model.Like;
import dev.mkopp.mysite.blog.infrastructure.adapter.out.persistence.entity.LikeEntity;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface LikeEntityMapper {
    
    Like toDomain(LikeEntity entity);
    
    LikeEntity toEntity(Like domain);
}
