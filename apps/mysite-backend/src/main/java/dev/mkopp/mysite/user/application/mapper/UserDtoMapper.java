package dev.mkopp.mysite.user.application.mapper;

import dev.mkopp.mysite.user.api.dto.UserDto;
import dev.mkopp.mysite.user.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserDtoMapper {
    
    UserDto toDto(User user);
}
