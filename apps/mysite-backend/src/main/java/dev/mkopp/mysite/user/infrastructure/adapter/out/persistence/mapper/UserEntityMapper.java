package dev.mkopp.mysite.user.infrastructure.adapter.out.persistence.mapper;

import dev.mkopp.mysite.user.domain.model.User;
import dev.mkopp.mysite.user.infrastructure.adapter.out.persistence.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserEntityMapper {
    
    User toDomain(UserEntity entity);
    
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserEntity toEntity(User user);
}
