package dev.mkopp.mysite.blog.infrastructure.adapter.out.persistence.entity;

import dev.mkopp.mysite.blog.domain.model.Like;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
interface LikeEntityMapper {
    
    Like toDomain(LikeEntity entity);
    
    LikeEntity toEntity(Like domain);
}
