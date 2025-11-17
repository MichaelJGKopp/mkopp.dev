package dev.mkopp.mysite.user.infrastructure.adapter.in.rest.mapper;

import dev.mkopp.mysite.user.domain.model.User;
import dev.mkopp.mysite.user.infrastructure.adapter.in.rest.dto.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserRestMapper {
    
    UserResponse toResponse(User user);
}
