package dev.mkopp.mysite.user.infrastructure.adapter.in.rest.mapper;

import java.time.Instant;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import dev.mkopp.mysite.user.domain.model.User;
import dev.mkopp.mysite.user.infrastructure.adapter.in.rest.dto.UserResponse;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserRestMapper {
    
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "tokenExpiry", ignore = true)
    UserResponse toResponse(User user);
    
    default UserResponse toResponse(User user, List<String> roles, Instant tokenExpiry) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            roles,
            tokenExpiry
        );
    }
}
