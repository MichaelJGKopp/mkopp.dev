package dev.mkopp.mysite.blog.infrastructure.adapter.out.user;

import dev.mkopp.mysite.blog.application.port.out.UserRepository.UserInfo;
import dev.mkopp.mysite.user.api.dto.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
interface UserInfoMapper {
    
    UserInfo toUserInfo(UserDto dto);
}
