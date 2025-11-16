package dev.mkopp.mysite.user.infrastructure.adapter.out.persistence.mapper;

import dev.mkopp.mysite.user.domain.model.User;
import dev.mkopp.mysite.user.infrastructure.adapter.out.persistence.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
interface UserEntityMapper {
    
    User toDomain(UserEntity entity);
    
    UserEntity toEntity(User user);
}
