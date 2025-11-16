package dev.mkopp.mysite.blog.application.mapper;

import dev.mkopp.mysite.blog.domain.model.Author;
import dev.mkopp.mysite.user.api.dto.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface AuthorMapper {
    
    Author toAuthor(UserDto userDto);
}
