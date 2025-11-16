package dev.mkopp.mysite.blog.infrastructure.adapter.out.persistence.entity;

import dev.mkopp.mysite.blog.domain.model.Author;
import dev.mkopp.mysite.user.api.dto.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
interface AuthorMapper {
    
    Author toAuthor(UserDto dto);
}
